package ee.toolrental.persistence.profile;

import ee.toolrental.controller.profile.dto.ProfileDto;
import ee.toolrental.persistence.appuser.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProfileMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "location.district.city.id", target = "cityId")
    @Mapping(source = "location.district.id", target = "districtId")
    @Mapping(source = "location.streetName", target = "streetName")
    @Mapping(source = "location.houseNumber", target = "houseNumber")
    @Mapping(source = "location.apartmentNumber", target = "apartmentNumber")
    @Mapping(constant = "true", target = "hasProfile")
    ProfileDto toProfileDto(Profile profile);

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(ignore = true, target = "email")
    @Mapping(constant = "false", target = "hasProfile")
    ProfileDto toProfileDto(AppUser appUser);
}
