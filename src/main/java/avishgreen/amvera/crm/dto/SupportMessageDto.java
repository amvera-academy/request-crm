package avishgreen.amvera.crm.dto;

import java.time.LocalDateTime;

public record SupportMessageDto(
        String text,
        LocalDateTime timestamp,
        String author
) {}