package ee.toolrental.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.util.Collection;

public class AppUserPrincipal extends DefaultOidcUser {

    private final Integer userId;

    public AppUserPrincipal(Integer userId, Collection<? extends GrantedAuthority> authorities,
                            OidcIdToken idToken, OidcUserInfo userInfo) {
        super(authorities, idToken, userInfo);
        this.userId = userId;
    }

    public Integer getUserId() {
        return userId;
    }
}