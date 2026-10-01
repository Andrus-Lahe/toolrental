package ee.toolrental.controller.booking.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingResponseDto {
    private Integer bookingId;
    private Integer toolId;
    private Integer renterId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String ownerMessage;
}
