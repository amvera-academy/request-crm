package avishgreen.amvera.crm.dto;

import avishgreen.amvera.crm.enums.SupportRequestStatus;
import avishgreen.amvera.crm.models.SupportRequestModel;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record SupportRequestDto(
        Long id,
        List<SupportMessageDto> messages,
        String author,
        List<String> participants,
        LocalDateTime lastUpdateTime,
        String note,
        SupportRequestStatus status
) {
    public SupportRequestDto(SupportRequestModel request) {
        this(
                request.getId(),
                request.getMessages(),
                request.getAuthor(),
                request.getParticipants(),
                request.getLastUpdateTime(),
                request.getNote(),
                request.getStatus()
        );
    }

    // Метод для получения текста последнего сообщения
    public String getLastMessageText() {
        return messages.stream()
                .max(Comparator.comparing(SupportMessageDto::timestamp))
                .map(SupportMessageDto::text)
                .orElse("Нет сообщений");
    }
}