package avishgreen.amvera.crm.dto;

import avishgreen.amvera.crm.enums.SupportRequestStatusType;

import java.time.Instant;
import java.util.List;

public record SupportRequestDto(
        Long id,
        Instant lastMessageAt,
        String lastMessageText,
        String note,
        Long lastMessageId,
        Long chatId,
        Long authorId,
        String authorName,
        SupportRequestStatusType status,
        List<TelegramMessageDto> messages,
        List<String> participantNames
) {
    /**
     * Создает новую копию SupportRequestDto с обновленным списком сообщений.
     */
    public SupportRequestDto withMessages(List<TelegramMessageDto> newMessages) {
        return new SupportRequestDto(
                this.id,
                this.lastMessageAt,
                this.lastMessageText,
                this.note,
                this.lastMessageId,
                this.chatId,
                this.authorId,
                this.authorName,
                this.status,
                newMessages, // <-- Здесь подставляем новый список
                this.participantNames
        );
    }
}