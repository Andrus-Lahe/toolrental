package ee.toolrental.persistence.profile;

import ee.toolrental.controller.profile.dto.ProfileDto;
import ee.toolrental.controller.profile.dto.ProfileUpdateRequestDto;
import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.location.Location;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProfileMapper {

    /**
     * Teisendab olemasoleva profiili koos kasutaja ja aadressiga ProfileDto-ks.
     * cityId võetakse linnaosa kaudu ja hasProfile on alati true.
     */
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

    /**
     * Teisendab profiilita kasutaja ProfileDto-ks, kus on ainult ID ja nimi.
     * E-post lisatakse service'is sessioonist, aadressi väljad jäävad null ja hasProfile on false.
     */
    @Mapping(source = "id", target = "userId")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(ignore = true, target = "email")
    @Mapping(constant = "false", target = "hasProfile")
    ProfileDto toProfileDto(AppUser appUser);

    /**
     * Loob päringu andmetest uue profiili, kus on täidetud e-post ja telefon.
     * Kasutaja, aadress ja ajatemplid lisatakse service'is.
     */
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    Profile toProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    /**
     * Kirjutab päringu e-posti ja telefoni olemasolevale profiilile üle.
     * Kasutaja, aadress ja created_at jäävad muutmata.
     */
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto, @MappingTarget Profile profile);

    /**
     * Loob päringu andmetest uue aadressi (tänav, majanumber, korter).
     * Linnaosa lisatakse service'is, lng ja lat jäävad null.
     */
    @Mapping(source = "streetName", target = "streetName")
    @Mapping(source = "houseNumber", target = "houseNumber")
    @Mapping(source = "apartmentNumber", target = "apartmentNumber", qualifiedByName = "toNullIfBlank")
    Location toLocation(ProfileUpdateRequestDto profileUpdateRequestDto);

    /**
     * Kirjutab päringu tänava, majanumbri ja korteri olemasolevale aadressile üle.
     * Linnaosa määratakse service'is, lng ja lat jäävad muutmata.
     */
    @Mapping(source = "streetName", target = "streetName")
    @Mapping(source = "houseNumber", target = "houseNumber")
    @Mapping(source = "apartmentNumber", target = "apartmentNumber", qualifiedByName = "toNullIfBlank")
    void updateLocation(ProfileUpdateRequestDto profileUpdateRequestDto, @MappingTarget Location location);

    /**
     * Muudab tühja või ainult tühikutest koosneva teksti null väärtuseks.
     * Nii salvestub puuduv korterinumber andmebaasi NULL-ina.
     */
    @Named("toNullIfBlank")
    default String toNullIfBlank(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
