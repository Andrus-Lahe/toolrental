package ee.toolrental.service;

import ee.toolrental.controller.profile.dto.ProfileDto;
import ee.toolrental.controller.profile.dto.ProfileUpdateRequestDto;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.appuser.AppUserMapper;
import ee.toolrental.persistence.district.District;
import ee.toolrental.persistence.location.Location;
import ee.toolrental.persistence.location.LocationRepository;
import ee.toolrental.persistence.profile.Profile;
import ee.toolrental.persistence.profile.ProfileMapper;
import ee.toolrental.persistence.profile.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ProfileService {

    private static final String PROFILE_LOADING_FAILED = "Profiili laadimine ebaõnnestus. Palun proovi hiljem uuesti.";
    private static final String PROFILE_SAVING_FAILED = "Profiili salvestamine ebaõnnestus. Palun proovi hiljem uuesti.";
    private static final String EMAIL_ALREADY_EXISTS_MESSAGE = "Sellise e-postiga kasutaja on juba süsteemis olemas";
    private static final String EMAIL_ALREADY_EXISTS = "EMAIL_ALREADY_EXISTS";

    private final ProfileRepository profileRepository;
    private final LocationRepository locationRepository;
    private final ProfileMapper profileMapper;
    private final AppUserMapper appUserMapper;
    private final AppUserService appUserService;
    private final DistrictService districtService;

    /**
     * Tagastab sisselogitud kasutaja profiili andmed MyProfile vormi täitmiseks.
     * Kui profiili veel pole, tuleb nimi app_user tabelist ja e-post Google'i sessioonist (hasProfile = false).
     * Andmebaasi tõrge muudetakse 500 vastuseks.
     */
    @Transactional(readOnly = true)
    public ProfileDto getProfile(Integer userId, String email) {
        try {
            Optional<Profile> optionalProfile = profileRepository.findProfileBy(userId);
            if (optionalProfile.isPresent()) {
                return profileMapper.toProfileDto(optionalProfile.get());
            }
            return getProfileFromAppUser(userId, email);
        } catch (DataAccessException e) {
            throw new InternalServerErrorException(PROFILE_LOADING_FAILED);
        }
    }

    /**
     * Salvestab sisselogitud kasutaja profiili ühes transaktsioonis: loob profiili ja aadressi või uuendab olemasolevaid.
     * Enne salvestamist kontrollitakse linnaosa olemasolu (404) ja e-posti vabadust (403).
     * Andmebaasi tõrge muudetakse 500 vastuseks ning kõik muudatused võetakse tagasi.
     */
    @Transactional
    public void updateProfile(Integer userId, ProfileUpdateRequestDto profileUpdateRequestDto) {
        try {
            District district = districtService.getValidDistrictBy(profileUpdateRequestDto.getDistrictId());
            validateEmailIsAvailable(profileUpdateRequestDto.getEmail(), userId);
            AppUser appUser = appUserService.getValidAppUserBy(userId);
            appUserMapper.updateAppUser(profileUpdateRequestDto, appUser);
            Optional<Profile> optionalProfile = profileRepository.findProfileBy(userId);
            handleSaveProfile(optionalProfile, profileUpdateRequestDto, appUser, district);
        } catch (DataAccessException e) {
            throw new InternalServerErrorException(PROFILE_SAVING_FAILED);
        }
    }

    /**
     * Koostab profiilita kasutaja vastuse app_user andmetest.
     * E-postiks pannakse Google'i sessioonist saadud aadress.
     */
    private ProfileDto getProfileFromAppUser(Integer userId, String email) {
        AppUser appUser = appUserService.getValidAppUserBy(userId);
        ProfileDto profileDto = profileMapper.toProfileDto(appUser);
        profileDto.setEmail(email);
        return profileDto;
    }

    /**
     * Kontrollib, et sama e-posti pole teise kasutaja profiilil.
     * Kasutaja enda senine e-post on lubatud; muidu visatakse ForbiddenException (403 EMAIL_ALREADY_EXISTS).
     */
    private void validateEmailIsAvailable(String email, Integer userId) {
        if (profileRepository.existsOtherUserProfileBy(email, userId)) {
            throw new ForbiddenException(EMAIL_ALREADY_EXISTS_MESSAGE, EMAIL_ALREADY_EXISTS);
        }
    }

    /**
     * Valib profiili salvestamise viisi selle järgi, kas kasutajal on profiil juba olemas.
     * Olemas: uuendatakse olemasolevat profiili ja aadressi; puudub: luuakse uued read.
     */
    private void handleSaveProfile(Optional<Profile> optionalProfile, ProfileUpdateRequestDto profileUpdateRequestDto,
                                   AppUser appUser, District district) {
        if (optionalProfile.isPresent()) {
            updateExistingProfile(optionalProfile.get(), profileUpdateRequestDto, district);
        } else {
            createNewProfile(profileUpdateRequestDto, appUser, district);
        }
    }

    /**
     * Uuendab olemasoleva profiili e-posti, telefoni ja updated_at väärtust ning profiili aadressi.
     * Uut aadressi ei looda; created_at, lng ja lat jäävad muutmata.
     */
    private void updateExistingProfile(Profile profile, ProfileUpdateRequestDto profileUpdateRequestDto, District district) {
        Location location = profile.getLocation();
        profileMapper.updateLocation(profileUpdateRequestDto, location);
        location.setDistrict(district);
        profileMapper.updateProfile(profileUpdateRequestDto, profile);
        profile.setUpdatedAt(Instant.now());
        profileRepository.saveAndFlush(profile);
    }

    /**
     * Loob kasutajale uue aadressi ja selle külge uue profiili.
     * created_at ja updated_at saavad praeguse aja, aadressi lng ja lat jäävad null.
     */
    private void createNewProfile(ProfileUpdateRequestDto profileUpdateRequestDto, AppUser appUser, District district) {
        Location location = profileMapper.toLocation(profileUpdateRequestDto);
        location.setDistrict(district);
        locationRepository.save(location);

        Profile profile = profileMapper.toProfile(profileUpdateRequestDto);
        profile.setUser(appUser);
        profile.setLocation(location);
        Instant now = Instant.now();
        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);
        profileRepository.saveAndFlush(profile);
    }

}
