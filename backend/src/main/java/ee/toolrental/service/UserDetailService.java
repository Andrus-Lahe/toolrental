package ee.toolrental.service;

import ee.toolrental.controller.appuser.dto.UserDetailResponse;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.appuser.AppUserMapper;
import ee.toolrental.persistence.appuser.UserDetailMapper;
import ee.toolrental.persistence.profile.Profile;
import ee.toolrental.persistence.profile.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailService {

    private static final String USER_DETAILS_LOADING_FAILED =
            "Kasutaja andmete laadimine ebaõnnestus. Palun proovi hiljem uuesti.";

    private final AppUserService appUserService;
    private final ProfileRepository profileRepository;
    private final UserDetailMapper userDetailMapper;

    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetails(Integer userId) {
        try {
            AppUser appUser = appUserService.getValidAppUserBy(userId);
            UserDetailResponse userDetailResponse = userDetailMapper.toUserDetailResponse(appUser);
            Optional<Profile> optionalProfile = profileRepository.findProfileByUserId(userId);
            handleProfile(userDetailResponse, optionalProfile);
            return userDetailResponse;
        } catch (DataAccessException exception) {
            throw new InternalServerErrorException(USER_DETAILS_LOADING_FAILED);
        }
    }

    private void handleProfile(UserDetailResponse userDetailResponse, Optional<Profile> optionalProfile) {
        if (optionalProfile.isPresent()) {
            Profile profile = optionalProfile.get();
            userDetailResponse.setEmail(profile.getEmail());
            userDetailResponse.setPhone(profile.getPhone());
        }
    }
}
