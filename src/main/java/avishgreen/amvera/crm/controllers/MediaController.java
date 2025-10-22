package avishgreen.amvera.crm.controllers;

import avishgreen.amvera.crm.exceptions.TelegramFileNotFoundException;
import avishgreen.amvera.crm.services.TelegramMediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.apache.commons.io.IOUtils; // Необходим для преобразования InputStream в byte[]

import java.io.IOException;
import java.io.InputStream;

@RestController // !!! Используем @RestController для отдачи данных напрямую !!!
@RequiredArgsConstructor
@Slf4j
public class MediaController {

    private final TelegramMediaService telegramMediaService;

    /**
     * Предоставляет бинарный контент медиафайла по его ID из нашей БД.
     * Возвращает изображение или ошибку (404, 503).
     * @param mediaId ID сущности TelegramMedia.
     * @return Ответ с содержимым файла и HTTP-заголовками.
     */
    @GetMapping("/media/{mediaId}")
    public ResponseEntity<byte[]> getMediaContent(@PathVariable Long mediaId) {

        try (InputStream inputStream = telegramMediaService.downloadMediaContent(mediaId)) {

            // Предполагаем, что для фото всегда отдаем JPEG.
            // В продакшене нужно получать MIME-тип из сущности TelegramMedia.

            byte[] mediaBytes = IOUtils.toByteArray(inputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(mediaBytes.length);

            return new ResponseEntity<>(mediaBytes, headers, HttpStatus.OK);

        } catch (TelegramFileNotFoundException e) {
            // Файл удален на стороне Telegram или помечен как удаленный (HTTP 404)
            log.warn("Attempt to access deleted mediaFiles (ID: {}).", mediaId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Медиафайл удален или не найден.", e);

        } catch (IllegalArgumentException e) {
            // Файл не найден в нашей БД
            log.warn("Media entity not found in DB (ID: {}).", mediaId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);

        } catch (IOException e) {
            // Сетевые ошибки, Telegram API недоступен, проблемы с I/O
            log.error("Failed to download mediaFiles content (ID: {}) due to IO/network error.", mediaId, e);
            // Возвращаем 503 Service Unavailable, указывая на временную проблему
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Сервер Telegram недоступен. Повторите попытку позже.", e);
        }
    }
}