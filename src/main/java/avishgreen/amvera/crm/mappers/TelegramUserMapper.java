package avishgreen.amvera.crm.mappers;

import avishgreen.amvera.crm.dto.TelegramUserDto;
import avishgreen.amvera.crm.entities.TelegramUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TelegramUserMapper {
    TelegramUserDto toDto(TelegramUser user);
    @Mapping(target = "lastUpdated", ignore = true)
    TelegramUser toEntity(TelegramUserDto dto);
}