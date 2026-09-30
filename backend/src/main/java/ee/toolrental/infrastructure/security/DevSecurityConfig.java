//package ee.toolrental.infrastructure.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.web.SecurityFilterChain;
//
///** Ajutine seadistus kohalikuks arenduseks ilma autentimiseta. */
//@Configuration
//@Profile("dev-no-auth")
//public class DevSecurityConfig {
//
//    @Bean
//    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
//        return http
//                .authorizeHttpRequests(requests -> requests.anyRequest().permitAll())
//                .csrf(AbstractHttpConfigurer::disable)
//                .build();
//    }
//}
