package avishgreen.amvera.crm.mappers;

import avishgreen.amvera.crm.dto.TelegramUserDto;
import avishgreen.amvera.crm.entities.TelegramUser;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TelegramUserMapper {
    TelegramUserDto toDto(TelegramUser user);
    TelegramUser toEntity(TelegramUserDto dto);
}