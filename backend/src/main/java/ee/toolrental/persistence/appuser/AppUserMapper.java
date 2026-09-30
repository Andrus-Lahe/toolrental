package ee.toolrental.persistence.appuser;

import ee.toolrental.controller.appuser.dto.CurrentUserDto;
import ee.toolrental.controller.profile.dto.ProfileUpdateRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AppUserMapper {

    /**
     * Teisendab kasutaja CurrentUserDto-ks (ID, roll, nimi).
     * E-post ja hasProfile täidetakse service'is profiili olemasolu järgi.
     */
    @Mapping(source = "id", target = "userId")
    @Mapping(source = "role.roleName", target = "roleName")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(ignore = true, target = "email")
    @Mapping(ignore = true, target = "hasProfile")
    CurrentUserDto toCurrentUserDto(AppUser appUser);

    /**
     * Kirjutab profiili salvestamise päringust kasutaja ees- ja perekonnanime üle.
     * Roll, status ja google_sub jäävad muutmata.
     */
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    void updateAppUser(ProfileUpdateRequestDto profileUpdateRequestDto, @MappingTarget AppUser appUser);
}
