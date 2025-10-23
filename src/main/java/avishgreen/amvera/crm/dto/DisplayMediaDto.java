package avishgreen.amvera.crm.dto;

import lombok.Builder;

// Этот DTO агрегирует информацию о PREVIEW и FULL_SIZE для одного файла
@Builder
public record DisplayMediaDto(
        // ID записи TelegramMedia для отображения миниатюры (PREVIEW)
        Long previewMediaId,

        // ID записи TelegramMedia для открытия полного размера (FULL_SIZE)
        Long fullsizeMediaId,

        // Общие свойства для отображения
        String fileUniqueId,
        String mimeType,
        Integer fileSize, // Размер оригинального файла
        Integer width,    // Ширина оригинального файла
        Integer height,   // Высота оригинального файла

        // Флаг, который показывает, что файл был удален Telegram (берется из PREVIEW-записи)
        Boolean deletedByTelegram
) {}