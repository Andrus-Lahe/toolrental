package ee.toolrental.controller.booking.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingApprovalDto {
    private Integer bookingId;
    private Integer toolId;
    private String toolName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String ownerMessage;
    private Boolean isOwner;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
}
