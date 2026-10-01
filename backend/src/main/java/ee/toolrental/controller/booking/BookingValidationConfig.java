package ee.toolrental.controller.booking;

import org.springframework.boot.validation.autoconfigure.ValidationConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class BookingValidationConfig {
    @Bean
    ValidationConfigurationCustomizer tallinnValidationClock() {
        return configuration -> configuration.clockProvider(() -> Clock.system(ZoneId.of("Europe/Tallinn")));
    }
}
