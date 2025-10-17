package avishgreen.amvera.crm.services;

import avishgreen.amvera.crm.entities.TelegramMedia;
import avishgreen.amvera.crm.entities.TelegramMessage;
import avishgreen.amvera.crm.enums.TelegramMediaUsageType;
import avishgreen.amvera.crm.exceptions.TelegramFileNotFoundException;
import avishgreen.amvera.crm.repositories.TelegramMediaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.photo.PhotoSize;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramMediaService {

    private final TelegramMediaRepository mediaRepository;
    private final TelegramApiService telegramApiService;

    /**
     * Обрабатывает медиафайлы, прикрепленные к сообщению, сохраняя метаданные
     * для PREVIEW (самый маленький размер) и FULL_SIZE (самый большой размер).
     *
     * @param message Объект сообщения от Telegram.
     * @param telegramMessage Сохраненная сущность сообщения в нашей БД.
     */
    @Transactional
    public void processMessageMedia(@NotNull Message message, @NotNull TelegramMessage telegramMessage) {
        // Проверяем наличие фотографий
        if (message.getPhoto() == null || message.getPhoto().isEmpty()) {
            return;
        }

        List<PhotoSize> photos = message.getPhoto();
        Optional<PhotoSize> largestPhotoOpt = findLargestPhoto(photos);
        Optional<PhotoSize> smallestPhotoOpt = findSmallestPhoto(photos);

        if (largestPhotoOpt.isEmpty() || smallestPhotoOpt.isEmpty()) {
            // Если по какой-то причине нет ни самого большого, ни самого маленького фото, выходим
            return;
        }

        PhotoSize smallestPhoto = smallestPhotoOpt.get();
        PhotoSize largestPhoto = largestPhotoOpt.get();

        // 1. Сохраняем PREVIEW версию (самую маленькую)
        saveMediaMetadata(smallestPhoto, telegramMessage, TelegramMediaUsageType.PREVIEW);

        // 2. Сохраняем FULL_SIZE версию (самую большую)
        // Проверяем, отличается ли FULL_SIZE от PREVIEW (по fileId, который уникален для каждой версии).
        // Если fileId одинаковые, значит, Telegram не предоставил разных разрешений.
        if (!largestPhoto.getFileId().equals(smallestPhoto.getFileId())) {
            saveMediaMetadata(largestPhoto, telegramMessage, TelegramMediaUsageType.FULL_SIZE);
        }

        // Обновляем флаг isMedia в родительском сообщении для оптимизации запросов.
        // Это требует, чтобы TelegramMessage был либо передан с актуальными данными,
        // либо был сохранен после этого в TelegramMessageService.
        if (Boolean.FALSE.equals(telegramMessage.getIsMedia())) {
            telegramMessage.setIsMedia(true);
            // Если telegramMessage - это управляемая сущность,
            // Spring автоматически сохранит это изменение при выходе из @Transactional.
        }
    }

    /**
     * Сохраняет метаданные одного PhotoSize в базу данных.
     */
    private void saveMediaMetadata(PhotoSize photoSize, TelegramMessage message, TelegramMediaUsageType usageType) {
        TelegramMedia media = TelegramMedia.builder()
                .telegramFileId(photoSize.getFileId())
                .fileUniqueId(photoSize.getFileUniqueId())
                // Для PhotoSize mimeType не передается явно, но это всегда JPEG
                .mimeType("image/jpeg")
                .fileSize(photoSize.getFileSize())
                .width(photoSize.getWidth())
                .height(photoSize.getHeight())
                .isDeletedByTelegram(false)
                .usageType(usageType)
                .message(message)
                .build();

        mediaRepository.save(media);
    }

    /**
     * Находит PhotoSize с самым большим размером файла (для FULL_SIZE).
     */
    private Optional<PhotoSize> findLargestPhoto(List<PhotoSize> photos) {
        return photos.stream()
                .max(Comparator.comparing(PhotoSize::getFileSize));
    }

    /**
     * Находит PhotoSize с самым маленьким размером файла (для PREVIEW).
     */
    private Optional<PhotoSize> findSmallestPhoto(List<PhotoSize> photos) {
        return photos.stream()
                .min(Comparator.comparing(PhotoSize::getFileSize));
    }

    /**
     * Вызывается из проксирующего контроллера для получения контента медиафайла.
     * Обрабатывает случаи, когда файл может быть удален с сервера Telegram.
     *
     * @param mediaId ID сущности TelegramMedia в нашей базе данных.
     * @return InputStream содержимого файла. Вызывающая сторона должна закрыть этот поток.
     * @throws IOException Если произошла сетевая ошибка или файл был удален.
     */
    @Transactional
    public InputStream downloadMediaContent(Long mediaId) throws IOException {
        // Найти метаданные файла в нашей БД
        TelegramMedia media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Медиафайл с ID " + mediaId + " не найден в БД."));

        // Проверить, не помечен ли файл как удаленный
        if (media.getIsDeletedByTelegram()) {
            log.warn("Attempt to download media with ID {} failed: file permanently deleted by Telegram.", mediaId);
            // Возвращаем специфическое исключение для 404
            throw new TelegramFileNotFoundException("Файл помечен как удаленный в нашей системе.");
        }

        // Получить file_path из Telegram API
        File telegramFile;
        try {
            // Используем fileId из нашей сущности для запроса метаданных в Telegram
            telegramFile = telegramApiService.getFile(media.getTelegramFileId());
        } catch (Exception e) {
            // Обработка любых ошибок при вызове getFile (например, API недоступен)
            log.error("Failed to get file metadata for media ID {} from Telegram.", mediaId, e);
            throw new IOException("Ошибка при получении метаданных файла из Telegram.", e);
        }

        // Проверка file_path
        if (telegramFile == null || telegramFile.getFilePath() == null || telegramFile.getFilePath().isEmpty()) {
            log.warn("Telegram returned file metadata without a file_path for ID: {}", mediaId);
            // Установим isDeletedByTelegram=true,
            // так как это неверное состояние, которое может быть перманентным.
            // Обработка: Файл удален на стороне Telegram (HTTP 404)
            media.setIsDeletedByTelegram(true); // Устанавливаем пометку
            mediaRepository.save(media); // Сохраняем изменение в транзакции
            throw new IOException("Telegram не вернул путь к файлу (file_path). Flagging entity");
        }

        // Скачать файл
        try {
            // Вызываем метод скачивания, который может выбросить TelegramFileNotFoundException
            return telegramApiService.downloadFile(telegramFile.getFilePath());

        } catch (TelegramFileNotFoundException e) {
            // Обработка: Файл удален на стороне Telegram (HTTP 404)
            media.setIsDeletedByTelegram(true); // Устанавливаем пометку
            mediaRepository.save(media); // Сохраняем изменение в транзакции
            log.warn("Media file ID {} has been permanently deleted by Telegram. Flagging entity.", mediaId);
            // Перебрасываем исключение, чтобы контроллер знал о проблеме 404
            throw e;

        } catch (IOException e) {
            // Сетевые ошибки (таймаут, Telegram недоступен и т.д.)
            // В этом случае НЕ ставим isDeletedByTelegram = true
            log.error("Network or IO error while downloading media ID {}.", mediaId, e);
            throw e; // Перебрасываем ошибку для обработки на уровне контроллера (например, 503 Service Unavailable)
        }
    }
}
