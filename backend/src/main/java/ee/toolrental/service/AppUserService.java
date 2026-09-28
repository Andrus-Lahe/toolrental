package ee.toolrental.service;

import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.appuser.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;

    public void getCurrentUser(Integer userId, String email) {
        AppUser validAppUserBy = getValidAppUserId(userId);
    }

    public AppUser getValidAppUserId(Integer userId) {
        return appUserRepository.findById(userId).orElseThrow(() -> new PrimaryKeyNotFoundException("userId", userId));
    }
}

