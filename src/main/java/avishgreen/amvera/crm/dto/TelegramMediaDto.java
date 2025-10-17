package avishgreen.amvera.crm.dto;

import lombok.Builder;

@Builder
public record TelegramMediaDto(
        // Временный ID для скачивания через Telegram API
        String telegramFileId,

        // Постоянный ID для дедупликации
        String fileUniqueId,

        String mimeType,
        Integer fileSize,
        Integer width,
        Integer height,

        // Флаг, который показывает, что файл был удален Telegram
        Boolean isDeletedByTelegram
) {}
