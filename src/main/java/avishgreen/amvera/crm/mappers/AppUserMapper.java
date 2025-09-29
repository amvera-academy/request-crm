package avishgreen.amvera.crm.mappers;

import avishgreen.amvera.crm.dto.AppUserDto;
import avishgreen.amvera.crm.entities.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppUserMapper {

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "isAccountNonExpired", ignore = true)
    @Mapping(target = "isAccountNonLocked", ignore = true)
    @Mapping(target = "isCredentialsNonExpired", ignore = true)
    @Mapping(target = "isEnabled", ignore = true)
    AppUser toEntity(AppUserDto appUserDto);

    @Mapping(target = "createdAt", ignore = true)
    AppUserDto toDto(AppUser appUser);
}