package avishgreen.amvera.crm.dto;

import java.util.List;

public record ModeratorsListDto(
        List<ModeratorUserDto> moderatorsUserIds
) {}
