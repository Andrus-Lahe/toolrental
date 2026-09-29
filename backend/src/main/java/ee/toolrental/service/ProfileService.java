package ee.toolrental.service;

import ee.toolrental.controller.profile.dto.ProfileDto;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.profile.Profile;
import ee.toolrental.persistence.profile.ProfileMapper;
import ee.toolrental.persistence.profile.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ProfileService {

    private static final String PROFILE_LOADING_FAILED = "Profiili laadimine ebaõnnestus. Palun proovi hiljem uuesti.";

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final AppUserService appUserService;

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

    private ProfileDto getProfileFromAppUser(Integer userId, String email) {
        AppUser appUser = appUserService.getValidAppUserBy(userId);
        ProfileDto profileDto = profileMapper.toProfileDto(appUser);
        profileDto.setEmail(email);
        return profileDto;
    }

}
