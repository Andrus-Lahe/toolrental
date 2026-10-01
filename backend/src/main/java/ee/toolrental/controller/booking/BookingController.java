package ee.toolrental.controller.booking;

import ee.toolrental.controller.booking.dto.BookingApprovalDto;
import ee.toolrental.controller.booking.dto.BookingCreateRequestDto;
import ee.toolrental.controller.booking.dto.BookingDecisionRequest;
import ee.toolrental.controller.booking.dto.BookingResponseDto;
import ee.toolrental.infrastructure.error.ApiError;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PatchMapping("/bookings/{bookingId}/confirm")
    @Operation(summary = "Broneeringu kinnitamine")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Broneering kinnitati"),
            @ApiResponse(responseCode = "400", description = "Vigane omaniku sõnum", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Kasutaja pole sisse logitud"),
            @ApiResponse(responseCode = "403", description = "Broneeringu kinnitamine on keelatud", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Broneeringut ei leitud", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Kinnitamine ebaõnnestus", content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public void confirmBooking(@AuthenticationPrincipal AppUserPrincipal principal,
                               @PathVariable Integer bookingId,
                               @RequestBody @Valid BookingDecisionRequest request) {
        bookingService.confirmBooking(principal.getUserId(), bookingId, request.ownerMessage());
    }

    @PatchMapping("/bookings/{bookingId}/reject")
    @Operation(summary = "Broneeringu tagasilükkamine")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Broneering lükati tagasi"),
            @ApiResponse(responseCode = "400", description = "Vigane omaniku sõnum", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Kasutaja pole sisse logitud"),
            @ApiResponse(responseCode = "403", description = "Broneeringu tagasilükkamine on keelatud", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Broneeringut ei leitud", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Tagasilükkamine ebaõnnestus", content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public void rejectBooking(@AuthenticationPrincipal AppUserPrincipal principal,
                              @PathVariable Integer bookingId,
                              @RequestBody @Valid BookingDecisionRequest request) {
        bookingService.rejectBooking(principal.getUserId(), bookingId, request.ownerMessage());
    }

    @GetMapping("/bookings/{bookingId}")
    @Operation(summary = "Broneeringu andmete päring")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Broneeringu andmed ja kontaktid", content = @Content(schema = @Schema(implementation = BookingApprovalDto.class))),
            @ApiResponse(responseCode = "400", description = "Vigane bookingId", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Kasutaja pole sisse logitud"),
            @ApiResponse(responseCode = "403", description = "Broneeringu vaatamine on keelatud", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Broneeringut ei leitud", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Broneeringu laadimine ebaõnnestus", content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public BookingApprovalDto getBooking(@AuthenticationPrincipal AppUserPrincipal principal,
                                         @PathVariable Integer bookingId) {
        return bookingService.getBooking(principal.getUserId(), bookingId);
    }

    @PostMapping("/bookings")
    @Operation(summary = "Laenutaotluse loomine")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Laenutaotlus loodi"),
            @ApiResponse(responseCode = "400", description = "Vigane sisend", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Kasutaja pole sisse logitud"),
            @ApiResponse(responseCode = "403", description = "Tööriista laenamine on keelatud", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Tööriista ei leitud", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Salvestamine ebaõnnestus", content = @Content(schema = @Schema(implementation = ApiError.class)))})
    public BookingResponseDto createBooking(@AuthenticationPrincipal AppUserPrincipal principal,
                                            @RequestBody @Valid BookingCreateRequestDto request) {
        return bookingService.createBooking(principal.getUserId(), request);
    }
}
