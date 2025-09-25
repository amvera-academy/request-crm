package avishgreen.amvera.crm.dto;

import lombok.Builder;

@Builder
public record UserNoteDto(
        Long id,
        String noteText,
        Long authorId,
        Long creatorId,
        String creatorUsername
) {}