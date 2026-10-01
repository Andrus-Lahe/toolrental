package ee.toolrental.controller.booking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingCreateRequestDto {
    @NotNull(message = "on kohustuslik")
    @Positive(message = "peab olema positiivne")
    private Integer toolId;

    @NotNull(message = "on kohustuslik")
    @FutureOrPresent(message = "ei tohi olla minevikus")
    private LocalDate startDate;

    @NotNull(message = "on kohustuslik")
    private LocalDate endDate;

    @Size(max = 500, message = "ei tohi ületada 500 märki")
    private String ownerMessage;
}
