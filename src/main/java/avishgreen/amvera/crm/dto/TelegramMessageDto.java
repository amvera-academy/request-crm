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
        String mediaGroupId,
        List<TelegramMediaDto> mediaFiles
) {
    // Вспомогательный метод для создания новой копии с измененным списком медиа
    public TelegramMessageDto withMediaFiles(List<TelegramMediaDto> newMediaList) {
        return new TelegramMessageDto(
                this.telegramMessageId(),
                this.messageText(),
                this.sender(),
                this.authorName(),
                this.chatId(),
                this.sentAt(),
                this.isEdited(),
                this.isMedia(),
                this.supportRequestId(),
                this.replyToMessageId(),
                this.mediaGroupId(),
                newMediaList // <-- Подставляем новый, отфильтрованный список
        );
    }
}