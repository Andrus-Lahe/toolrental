package ee.toolrental.persistence.appuser;

import ee.toolrental.controller.appuser.dto.AppUserDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AppUserMapper {
    @Mapping(source = "roleRoleName", target = "role.roleName")
    @Mapping(source = "roleId", target = "role.id")
    AppUser toEntity(AppUserDto appUserDto);

    @InheritInverseConfiguration(name = "toEntity")
    AppUserDto toDto(AppUser appUser);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    AppUser partialUpdate(AppUserDto appUserDto, @MappingTarget AppUser appUser);
}