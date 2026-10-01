package ee.toolrental.service;

import ee.toolrental.controller.booking.dto.BookingCreateRequestDto;
import ee.toolrental.controller.booking.dto.BookingResponseDto;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.IncorrectInputException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.infrastructure.mail.BookingRequestMailService;
import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.booking.Booking;
import ee.toolrental.persistence.booking.BookingMapperImpl;
import ee.toolrental.persistence.booking.BookingRepository;
import ee.toolrental.persistence.tool.Tool;
import ee.toolrental.persistence.tool.ToolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookingServiceTest {
    private BookingRepository bookingRepository;
    private ToolRepository toolRepository;
    private AppUserService appUserService;
    private BookingRequestMailService mailService;
    private BookingService service;
    private Tool tool;
    private BookingCreateRequestDto request;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        toolRepository = mock(ToolRepository.class);
        appUserService = mock(AppUserService.class);
        mailService = mock(BookingRequestMailService.class);
        service = new BookingService(bookingRepository, toolRepository, new BookingMapperImpl(), appUserService, mailService);

        AppUser owner = new AppUser();
        owner.setId(1);
        tool = new Tool();
        tool.setId(5);
        tool.setName("Muruniiduk");
        tool.setOwner(owner);
        tool.setStatus("A");
        when(toolRepository.findToolForBookingBy(5)).thenReturn(Optional.of(tool));

        AppUser renter = new AppUser();
        renter.setId(3);
        when(appUserService.getValidAppUserBy(3)).thenReturn(renter);

        request = new BookingCreateRequestDto();
        request.setToolId(5);
        request.setStartDate(LocalDate.now(ZoneId.of("Europe/Tallinn")).plusDays(1));
        request.setEndDate(request.getStartDate().plusDays(2));
        request.setOwnerMessage("Sooviksin muruniidukit.");
        when(bookingRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(4);
            return booking;
        });
    }

    @Test
    void createsPendingBookingForSessionRenterAndSendsMail() {
        BookingResponseDto response = service.createBooking(3, request);

        assertEquals(4, response.getBookingId());
        assertEquals(5, response.getToolId());
        assertEquals(3, response.getRenterId());
        assertEquals("P", response.getStatus());
        assertEquals(request.getOwnerMessage(), response.getOwnerMessage());
        verify(bookingRepository).saveAndFlush(argThat(booking ->
                booking.getRenter().getId() == 3 && booking.getGoogleEventId() == null
                        && booking.getCreatedAt() != null && booking.getUpdatedAt() != null));
        verify(mailService).sendBookingRequest(any(Booking.class));
    }

    @Test
    void todayAndSameDayBookingAreAllowed() {
        request.setStartDate(LocalDate.now(ZoneId.of("Europe/Tallinn")));
        request.setEndDate(request.getStartDate());
        assertDoesNotThrow(() -> service.createBooking(3, request));
    }

    @Test
    void pastDateAndReversedPeriodAreRejectedBeforeLookup() {
        request.setStartDate(LocalDate.now(ZoneId.of("Europe/Tallinn")).minusDays(1));
        IncorrectInputException past = assertThrows(IncorrectInputException.class, () -> service.createBooking(3, request));
        assertEquals("startDate: ei tohi olla minevikus", past.getMessage());

        request.setStartDate(LocalDate.now(ZoneId.of("Europe/Tallinn")).plusDays(2));
        request.setEndDate(request.getStartDate().minusDays(1));
        IncorrectInputException reversed = assertThrows(IncorrectInputException.class, () -> service.createBooking(3, request));
        assertEquals("endDate: peab olema startDate'iga samal päeval või hiljem", reversed.getMessage());
        verifyNoInteractions(bookingRepository, mailService);
    }

    @Test
    void missingToolReturns404WithoutSaving() {
        request.setToolId(123);
        PrimaryKeyNotFoundException error = assertThrows(PrimaryKeyNotFoundException.class, () -> service.createBooking(3, request));
        assertEquals("Ei leidnud primary keyd 'toolId' väärtusega: 123", error.getMessage());
        verifyNoInteractions(bookingRepository, mailService);
    }

    @Test
    void ownToolHasPriorityOverUnavailabilityAndOverlap() {
        tool.setStatus("U");
        ForbiddenException error = assertThrows(ForbiddenException.class, () -> service.createBooking(1, request));
        assertEquals("OWN_TOOL_BOOKING_FORBIDDEN", error.getErrorCode());
        verifyNoInteractions(bookingRepository, mailService);
    }

    @Test
    void unavailableToolCannotBeBooked() {
        tool.setStatus("U");
        ForbiddenException error = assertThrows(ForbiddenException.class, () -> service.createBooking(3, request));
        assertEquals("TOOL_UNAVAILABLE", error.getErrorCode());
        verifyNoInteractions(bookingRepository, mailService);
    }

    @Test
    void overlappingActiveBookingCannotBeBooked() {
        when(bookingRepository.existsActiveBookingOverlapping(5, request.getStartDate(), request.getEndDate())).thenReturn(true);
        ForbiddenException error = assertThrows(ForbiddenException.class, () -> service.createBooking(3, request));
        assertEquals("TOOL_ALREADY_BOOKED", error.getErrorCode());
        verify(bookingRepository, never()).saveAndFlush(any());
        verifyNoInteractions(mailService);
    }

    @Test
    void databaseFailureReturnsContractErrorAndDoesNotSendMail() {
        doThrow(new DataAccessResourceFailureException("DB down")).when(bookingRepository).saveAndFlush(any());
        InternalServerErrorException error = assertThrows(InternalServerErrorException.class, () -> service.createBooking(3, request));
        assertEquals("INTERNAL_SERVER_ERROR", error.getErrorCode());
        assertEquals("Laenutuse taotluse saatmine ebaõnnestus. Palun proovi hiljem uuesti.", error.getMessage());
        verifyNoInteractions(mailService);
    }
}
