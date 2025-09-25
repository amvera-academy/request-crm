package avishgreen.amvera.crm.dto;

import avishgreen.amvera.crm.enums.SupportRequestStatusType;

import java.time.Instant;
import java.util.List;

public record SupportRequestReviewDto(
        Long id,
        Instant lastMessageAt,
        String lastMessageText,
        String information,
        Long lastMessageId,
        Long chatId,
        SupportRequestStatusType status
) {}