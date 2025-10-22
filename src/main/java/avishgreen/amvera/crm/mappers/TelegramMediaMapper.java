package avishgreen.amvera.crm.mappers;

import avishgreen.amvera.crm.dto.TelegramMediaDto;
import avishgreen.amvera.crm.entities.TelegramMedia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TelegramMediaMapper {
    @Mapping(target = "deletedByTelegram", source = "isDeletedByTelegram")
    TelegramMediaDto toDto(TelegramMedia media);
    List<TelegramMediaDto> toDtoList(List<TelegramMedia> mediaList);
    TelegramMedia toEntity(TelegramMediaDto dto);
}
