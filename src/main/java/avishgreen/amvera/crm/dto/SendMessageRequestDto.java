package avishgreen.amvera.crm.dto;

/**
 * Record для приема данных при отправке ответного сообщения модератора.
 */
public record SendMessageRequestDto(
        Long supportRequestId,
        String text
) {}