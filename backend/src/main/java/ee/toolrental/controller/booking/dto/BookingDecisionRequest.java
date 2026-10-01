package ee.toolrental.controller.booking.dto;

import jakarta.validation.constraints.Size;

public record BookingDecisionRequest(
        @Size(max = 500, message = "Sõnum võib olla kuni 500 märki")
        String ownerMessage
) {
}
