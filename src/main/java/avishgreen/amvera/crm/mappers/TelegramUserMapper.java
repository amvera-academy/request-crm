package avishgreen.amvera.crm.mappers;

import avishgreen.amvera.crm.dto.TelegramUserDto;
import avishgreen.amvera.crm.entities.TelegramUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TelegramUserMapper {
    TelegramUserDto toDto(TelegramUser user);
    @Mapping(target = "lastUpdated", ignore = true)
    TelegramUser toEntity(TelegramUserDto dto);
}