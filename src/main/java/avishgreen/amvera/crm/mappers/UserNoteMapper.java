package avishgreen.amvera.crm.mappers;

import avishgreen.amvera.crm.dto.UserNoteDto;
import avishgreen.amvera.crm.entities.AppUser;
import avishgreen.amvera.crm.entities.UserNote;
import avishgreen.amvera.crm.services.AppUserService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public interface UserNoteMapper {

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "creatorId", source = "creator.id")
    @Mapping(target = "creatorUsername", source = "creator.username")
    UserNoteDto toDto(UserNote userNote);

    @Mapping(target = "author", ignore = true)
    @Mapping(target = "creator", ignore = true)
    UserNote toEntity(UserNoteDto userNoteDto);

}