package avishgreen.amvera.crm.dto;

import avishgreen.amvera.crm.enums.TelegramMediaUsageType;
import lombok.Builder;

@Builder
public record TelegramMediaDto(
        // Временный ID для скачивания через Telegram API
        Long id,
        // Временный ID для скачивания через Telegram API
        String telegramFileId,

        // Постоянный ID для дедупликации
        String fileUniqueId,

        String mimeType,
        Integer fileSize,
        Integer width,
        Integer height,

        // Флаг, который показывает, что файл был удален Telegram
        Boolean deletedByTelegram,
        TelegramMediaUsageType usageType
) {}
