package avishgreen.amvera.crm.mappers;

import avishgreen.amvera.crm.dto.AppUserDto;
import avishgreen.amvera.crm.entities.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppUserMapper {

    @Mapping(target = "passwordHash", ignore = true)
    AppUser toEntity(AppUserDto appUserDto);

    AppUserDto toDto(AppUser appUser);
}