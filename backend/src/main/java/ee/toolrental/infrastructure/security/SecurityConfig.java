package ee.toolrental.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

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
                        .requestMatchers(HttpMethod.POST, "/api/tools").hasRole("customer")
                        .requestMatchers(HttpMethod.GET, "/api/users/me/tools").hasAnyRole("customer", "admin")
                        .anyRequest().authenticated())
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(appUserOidcService))
                        .defaultSuccessUrl(FRONTEND_URL, true)
                        .failureUrl(FRONTEND_URL + "?loginError"))
                .exceptionHandling(e -> e.authenticationEntryPoint((request, response, exception) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    if (HttpMethod.GET.matches(request.getMethod())
                            && request.getRequestURI().equals(request.getContextPath() + "/api/users/me/tools")) {
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write(
                                "{\"errorCode\":\"AUTHENTICATION_REQUIRED\",\"message\":\"Vaate avamiseks logi sisse.\"}");
                    }
                }))
                .logout(logout -> logout.logoutSuccessUrl(FRONTEND_URL))
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
