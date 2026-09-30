package ee.toolrental.persistence.appuser;

import ee.toolrental.controller.appuser.dto.CurrentUserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AppUserMapper {

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "role.roleName", target = "roleName")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(ignore = true, target = "email")
    @Mapping(ignore = true, target = "hasProfile")
    CurrentUserDto toCurrentUserDto(AppUser appUser);
}