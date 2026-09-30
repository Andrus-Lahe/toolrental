package ee.toolrental.persistence.district;

import ee.toolrental.controller.district.dto.DistrictDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface DistrictMapper {
    @Mapping(source = "id", target = "districtId")
    @Mapping(source = "districtName", target = "districtName")
    DistrictDto toDistrictDto(District district);

    List<DistrictDto> toDistrictDtos(List<District> districts);
}
