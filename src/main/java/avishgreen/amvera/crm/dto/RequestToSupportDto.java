package avishgreen.amvera.crm.dto;

import avishgreen.amvera.crm.enums.SupportRequestStatusType;
import avishgreen.amvera.crm.models.SupportRequestModel;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record RequestToSupportDto(
        Long id,
        List<MessageToSupportDto> messages,
        String author,
        List<String> participants,
        LocalDateTime lastUpdateTime,
        String note,
        SupportRequestStatusType status
) {
    public RequestToSupportDto(SupportRequestModel request) {
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
                .max(Comparator.comparing(MessageToSupportDto::timestamp))
                .map(MessageToSupportDto::text)
                .orElse("Нет сообщений");
    }
}