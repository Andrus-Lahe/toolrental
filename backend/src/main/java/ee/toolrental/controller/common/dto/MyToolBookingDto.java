package ee.toolrental.controller.common.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MyToolBookingDto {
    private Integer toolId;
    private String toolName;
    private String toolStatus;
    private String imageData;
    private Integer bookingId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String bookingStatus;
}
