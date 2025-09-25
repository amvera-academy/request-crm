package avishgreen.amvera.crm.dto;

import java.time.Instant;

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
        Long replyToMessageId
) {}