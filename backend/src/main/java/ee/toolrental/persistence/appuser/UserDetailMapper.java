package ee.toolrental.persistence.appuser;

import ee.toolrental.controller.appuser.dto.UserDetailResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserDetailMapper {

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(ignore = true, target = "email")
    @Mapping(ignore = true, target = "phone")
    UserDetailResponse toUserDetailResponse(AppUser appUser);
}
