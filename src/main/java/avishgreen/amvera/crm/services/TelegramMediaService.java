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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramMediaService {

    private final TelegramMediaRepository mediaRepository;
    private final TelegramApiService telegramApiService;
    // Интервал задержки в минутах, после которого разрешается новая попытка
    private static final long BASE_RETRY_INTERVAL_MINUTES = 10;

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

        // Генерируем уникальный ID для логической медиа-группы
        final String mediaGroupUuid = UUID.randomUUID().toString();

        // 1. Сохраняем PREVIEW версию
        var photoSize = smallestPhoto;
        var usageType = TelegramMediaUsageType.PREVIEW;

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
                .message(telegramMessage)
                .mediaGroupUuid(mediaGroupUuid)
                .build();

        mediaRepository.save(media);

        // 2. Сохраняем FULL_SIZE версию (самую большую)
        // Проверяем, отличается ли FULL_SIZE от PREVIEW (по fileId, который уникален для каждой версии).
        // Если fileId одинаковые, значит, Telegram не предоставил разных разрешений.
        if (!largestPhoto.getFileId().equals(smallestPhoto.getFileId())) {
            photoSize = largestPhoto;
            usageType = TelegramMediaUsageType.FULL_SIZE;

            media = TelegramMedia.builder()
                    .telegramFileId(photoSize.getFileId())
                    .fileUniqueId(photoSize.getFileUniqueId())
                    // Для PhotoSize mimeType не передается явно, но это всегда JPEG
                    .mimeType("image/jpeg")
                    .fileSize(photoSize.getFileSize())
                    .width(photoSize.getWidth())
                    .height(photoSize.getHeight())
                    .isDeletedByTelegram(false)
                    .usageType(usageType)
                    .message(telegramMessage)
                    .mediaGroupUuid(mediaGroupUuid)

                    .build();

            mediaRepository.save(media);
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
     * * Реализует логику задержки повторных попыток (back-off) и счетчик неудач.
     *
     * @param mediaId ID сущности TelegramMedia в нашей базе данных.
     * @return InputStream содержимого файла. Вызывающая сторона должна закрыть этот поток.
     * @throws IOException Если произошла сетевая ошибка или файл был удален.
     */
    @Transactional
    public InputStream downloadMediaContent(Long mediaId) throws IOException {
        // 1. Найти метаданные файла в нашей БД
        TelegramMedia media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Медиафайл с ID " + mediaId + " не найден в БД."));

        // 2. Проверка Блокировки (на основе растущего таймаута)
        if (Boolean.TRUE.equals(media.getIsDeletedByTelegram())) {

            // Если файл помечен как удаленный (временный флаг "недоступно")
            if (media.getRetryCount() > 0 && media.getLastAttemptAt() != null) {

                if (isRetryBlocked(media)) {
                    // Таймаут ещё не прошёл, возвращаем ошибку.
                    long minutesRemaining = remainsDelayMinutes(media);

                    log.info("Media ID {} is blocked for retry (attempt #{}). Blocking for {} more minutes.",
                            mediaId, media.getRetryCount(), minutesRemaining);

                    throw new IOException("Файл временно недоступен. Повторная попытка разрешена через примерно "
                            + minutesRemaining + " минут.");
                }

                // ЕСЛИ ТАЙМАУТ ПРОШЕЛ, ПЕРЕХОДИМ К ПОПЫТКЕ ЗАГРУЗКИ (3)
                log.info("Media ID {} backoff period ended after {} minutes. Attempting retry...", mediaId, countDelayMinutes(media));
            }
        }

        // --- 3. Попытка взаимодействия с Telegram API ---

        try {
            // A. Получить file_path из Telegram API и Скачать файл
            File telegramFile = telegramApiService.getFile(media.getTelegramFileId());

            if (telegramFile == null || telegramFile.getFilePath() == null || telegramFile.getFilePath().isEmpty()) {
                // Если нет пути, это ошибка, которую нужно обработать как неудачную попытку
                var ioEx = new IOException("Telegram returned file metadata without a file_path.");
                handleFailedAttempt(media, ioEx);
                throw ioEx;
            }

            InputStream fileStream = telegramApiService.downloadFile(telegramFile.getFilePath());

            // 4. УСПЕХ: Сбросить все флаги
            if (Boolean.TRUE.equals(media.getIsDeletedByTelegram())) {
                media.setIsDeletedByTelegram(false); // Сброс временной пометки
                media.setRetryCount(0);             // Сброс счетчика
                media.setLastAttemptAt(null);       // Сброс времени
                mediaRepository.save(media);
                log.info("Media ID {} successfully downloaded. Entity flags reset.", mediaId);
            }

            return fileStream;

        } catch (Exception e) {
            // 5. ОШИБКА: Файл не найден (404/410) или Сетевая/IO ошибка/Таймаут
            handleFailedAttempt(media, e);

            // Перебрасываем исключение
            if (e instanceof TelegramFileNotFoundException) {
                throw e; // 404/410 от Telegram
            } else {
                throw new IOException("Ошибка при взаимодействии с API Telegram.", e);
            }
        }
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ---

    /**
     * Проверяет, прошло ли необходимое время задержки с последней попытки.
     */
    public boolean isRetryBlocked(@NotNull TelegramMedia media) {
        var lastAttempt = media.getLastAttemptAt();
        var nextDelayMinutes = countDelayMinutes(media);

        if (lastAttempt == null) {
            return false;
        }
        return lastAttempt.plus(nextDelayMinutes, ChronoUnit.MINUTES).isAfter(Instant.now());
    }

    //Считаем экспоненциальную задержку
    private long countDelayMinutes(@NotNull TelegramMedia media) {
        var retryCount = media.getRetryCount()+1;

        return (long) retryCount*retryCount*BASE_RETRY_INTERVAL_MINUTES;
    }

    //Считаем сколько еще осталось минут задержки
    private long remainsDelayMinutes(@NotNull TelegramMedia media) {
        long minutesPassed = ChronoUnit.MINUTES.between(media.getLastAttemptAt(), Instant.now());
        long minutesRemaining = countDelayMinutes(media) - minutesPassed;
        return minutesRemaining;
    }

    /**
     * Обрабатывает неудачную попытку: помечает как временно удаленный, увеличивает счетчик и обновляет время.
     */
    private void handleFailedAttempt(@NotNull TelegramMedia media, @NotNull Exception exception) {

        // Всегда помечаем файл как временно недоступный/удаленный при неудаче.
        media.setIsDeletedByTelegram(true);

        int newCount = media.getRetryCount() + 1;
        media.setRetryCount(newCount);
        media.setLastAttemptAt(Instant.now());

        long nextDelayMinutes = countDelayMinutes(media);

        log.warn("Media ID {} failed attempt #{}. Blocking for {} minutes until next try.",
                media.getId(), newCount, nextDelayMinutes, exception);

        mediaRepository.save(media);
    }
}
