package ee.toolrental.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String FRONTEND_URL = "http://localhost:8081/";

    private final AppUserOidcService appUserOidcService;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/tools/**", "/api/categories/**", "/api/cities/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("admin")
                        .requestMatchers(HttpMethod.POST, "/api/bookings").hasAnyRole("customer", "admin")
                        .anyRequest().authenticated())
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(appUserOidcService))
                        .defaultSuccessUrl(FRONTEND_URL, true)
                        .failureUrl(FRONTEND_URL + "?loginError"))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .logout(logout -> logout.logoutSuccessUrl(FRONTEND_URL))
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
