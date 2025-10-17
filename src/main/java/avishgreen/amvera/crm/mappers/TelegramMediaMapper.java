package avishgreen.amvera.crm.mappers;

import avishgreen.amvera.crm.dto.TelegramMediaDto;
import avishgreen.amvera.crm.entities.TelegramMedia;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TelegramMediaMapper {
    TelegramMediaDto toDto(TelegramMedia media);
    List<TelegramMediaDto> toDtoList(List<TelegramMedia> mediaList);
    TelegramMedia toEntity(TelegramMediaDto dto);
}
