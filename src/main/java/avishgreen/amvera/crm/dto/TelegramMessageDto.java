package avishgreen.amvera.crm.dto;

import java.time.Instant;
import java.util.List;

public record TelegramMessageDto(
        Long telegramMessageId,
        String messageText,
        TelegramUserDto sender,
        String authorName,
        Long chatId,
        Instant sentAt,
        Boolean isEdited,
        Boolean isMedia,
        Long supportRequestId,
        Long replyToMessageId,
        Long previewMediaId,
        Long fullSizeMediaId
) {}