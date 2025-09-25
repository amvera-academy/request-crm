package avishgreen.amvera.crm.dto;

import java.time.LocalDateTime;

public record MessageToSupportDto(
        String text,
        LocalDateTime timestamp,
        String author
) {}