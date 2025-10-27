package avishgreen.amvera.crm.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AppUserDto(
        Long id,
        String username,
        String role,
        LocalDateTime createdAt,
        String botToken
) {}