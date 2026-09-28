package ee.toolrental.service;

import ee.toolrental.controller.appuser.dto.CurrentUserDto;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.appuser.AppUserMapper;
import ee.toolrental.persistence.appuser.AppUserRepository;
import ee.toolrental.persistence.profile.Profile;
import ee.toolrental.persistence.profile.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final AppUserMapper appUserMapper;
    private final ProfileRepository profileRepository;

    public void getCurrentUser(Integer userId, String email) {
        AppUser appUser = getValidAppUserBy(userId);
        CurrentUserDto currentUserDto = appUserMapper.toCurrentUserDto(appUser);
        Optional<Profile> optionalProfile = profileRepository.findProfileBy(userId);
        handleProfile(userId, email, Profile);

    }




    public AppUser getValidAppUserBy(Integer userId) {
        return appUserRepository.findById(userId).orElseThrow(() -> new PrimaryKeyNotFoundException("userId", userId));
    }
}



