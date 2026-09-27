package ee.toolrental.infrastructure.security;

import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.appuser.AppUserRepository;
import ee.toolrental.persistence.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.View;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AppUserOidcService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private static final int ROLE_CUSTOMER_ID = 2;
    private static final String STATUS_BLOCKED = "B";

    private final OidcUserService delegate = new OidcUserService();
    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final View error;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser googleUser = delegate.loadUser(userRequest);

        AppUser appUser = appUserRepository.findAppUserBy(googleUser.getSubject())
                .orElseGet(() -> createAppUser(googleUser));

        if (STATUS_BLOCKED.equals(appUser.getStatus())) {
            throw new OAuth2AuthenticationException(new OAuth2Error("user_blocked"), "Kasutaja on blokeeritud");
        }

        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().getRoleName()));

        return new AppUserPrincipal(appUser.getId(), authorities, googleUser.getIdToken(), googleUser.getUserInfo());
    }

    private AppUser createAppUser(OidcUser googleUser) {


        AppUser appUser = new AppUser();
        appUser.setGoogleSub(googleUser.getSubject());

        String givenName = googleUser.getGivenName();
        if (givenName == null || givenName.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("user_noname"), "Eesnimi on nõutud");
        }
        appUser.setFirstName(givenName);
        appUser.setLastName(Objects.requireNonNullElse(googleUser.getFamilyName(), ""));
        appUser.setRole(roleRepository.getReferenceById(ROLE_CUSTOMER_ID));
        appUser.setStatus("A");
        return appUserRepository.save(appUser);
    }


}

