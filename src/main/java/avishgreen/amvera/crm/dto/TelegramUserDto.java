package avishgreen.amvera.crm.dto;

import java.time.Instant;

public record TelegramUserDto(
        Long id,
        String username,
        String firstName,
        String lastName,
        Instant lastActivity
) {}