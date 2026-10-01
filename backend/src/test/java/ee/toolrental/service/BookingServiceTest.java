package ee.toolrental.service;

import ee.toolrental.controller.booking.dto.BookingCreateRequestDto;
import ee.toolrental.controller.booking.dto.BookingResponseDto;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.IncorrectInputException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.infrastructure.mail.BookingRequestMailService;
import ee.toolrental.infrastructure.mail.BookingDecisionMailService;
import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.booking.Booking;
import ee.toolrental.persistence.booking.BookingMapperImpl;
import ee.toolrental.persistence.booking.BookingRepository;
import ee.toolrental.persistence.tool.Tool;
import ee.toolrental.persistence.tool.ToolRepository;
import ee.toolrental.persistence.profile.Profile;
import ee.toolrental.persistence.profile.ProfileRepository;
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
    private BookingDecisionMailService decisionMailService;
    private ProfileRepository profileRepository;
    private BookingService service;
    private Tool tool;
    private BookingCreateRequestDto request;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        toolRepository = mock(ToolRepository.class);
        appUserService = mock(AppUserService.class);
        mailService = mock(BookingRequestMailService.class);
        decisionMailService = mock(BookingDecisionMailService.class);
        profileRepository = mock(ProfileRepository.class);
        service = new BookingService(bookingRepository, toolRepository, new BookingMapperImpl(), appUserService, mailService, profileRepository, decisionMailService);

        AppUser owner = new AppUser();
        owner.setId(1);
        owner.setFirstName("Marko");
        owner.setLastName("Tamm");
        tool = new Tool();
        tool.setId(5);
        tool.setName("Muruniiduk");
        tool.setOwner(owner);
        tool.setStatus("A");
        when(toolRepository.findToolForBookingBy(5)).thenReturn(Optional.of(tool));

        AppUser renter = new AppUser();
        renter.setId(3);
        renter.setFirstName("Liis");
        renter.setLastName("Kask");
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
        when(bookingRepository.findBookingWithPartiesById(4)).thenReturn(Optional.of(booking()));
    }

    @Test
    void ownerAndRenterSeeOtherPartyContactDetails() {
        Booking booking = booking();
        Profile renterProfile = new Profile();
        renterProfile.setEmail("liis@example.com");
        renterProfile.setPhone("55501002");
        when(profileRepository.findProfileByUserId(3)).thenReturn(Optional.of(renterProfile));
        var ownerResponse = service.getBooking(1, 4);
        assertEquals(true, ownerResponse.getIsOwner());
        assertEquals("Liis Kask", ownerResponse.getContactName());
        assertEquals("liis@example.com", ownerResponse.getContactEmail());
        assertEquals("55501002", ownerResponse.getContactPhone());

        Profile ownerProfile = new Profile();
        ownerProfile.setEmail("marko@example.com");
        ownerProfile.setPhone("56565656");
        when(profileRepository.findProfileByUserId(1)).thenReturn(Optional.of(ownerProfile));
        var renterResponse = service.getBooking(3, 4);
        assertEquals(false, renterResponse.getIsOwner());
        assertEquals("Marko Tamm", renterResponse.getContactName());
        assertEquals("marko@example.com", renterResponse.getContactEmail());
        assertEquals("56565656", renterResponse.getContactPhone());
        verify(bookingRepository, times(2)).findBookingWithPartiesById(4);
    }

    @Test
    void bookingWithoutOtherPartyProfileReturnsNullContactFieldsAndPreservesNullMessage() {
        Booking booking = booking();
        when(profileRepository.findProfileByUserId(3)).thenReturn(Optional.empty());
        var response = service.getBooking(1, 4);
        assertEquals("Liis Kask", response.getContactName());
        assertNull(response.getContactEmail());
        assertNull(response.getContactPhone());
        assertNull(response.getOwnerMessage());
    }

    @Test
    void missingBookingIs404BeforeCheckingAccessAndOtherUserIsForbidden() {
        when(bookingRepository.findBookingWithPartiesById(404)).thenReturn(Optional.empty());
        PrimaryKeyNotFoundException missing = assertThrows(PrimaryKeyNotFoundException.class, () -> service.getBooking(99, 404));
        assertEquals("Ei leidnud primary keyd 'bookingId' väärtusega: 404", missing.getMessage());

        when(bookingRepository.findBookingWithPartiesById(4)).thenReturn(Optional.of(booking()));
        ForbiddenException forbidden = assertThrows(ForbiddenException.class, () -> service.getBooking(99, 4));
        assertEquals("BOOKING_ACCESS_DENIED", forbidden.getErrorCode());
        verifyNoInteractions(profileRepository);
    }

    @Test
    void bookingReadDatabaseFailureUsesContractError() {
        when(bookingRepository.findBookingWithPartiesById(4)).thenThrow(new DataAccessResourceFailureException("DB down"));
        InternalServerErrorException error = assertThrows(InternalServerErrorException.class, () -> service.getBooking(1, 4));
        assertEquals("INTERNAL_SERVER_ERROR", error.getErrorCode());
        assertEquals("Broneeringu laadimine ebaõnnestus. Palun proovi hiljem uuesti.", error.getMessage());
    }

    private Booking booking() {
        Booking booking = new Booking();
        booking.setId(4);
        booking.setTool(tool);
        AppUser renter = new AppUser();
        renter.setId(3);
        renter.setFirstName("Liis");
        renter.setLastName("Kask");
        booking.setRenter(renter);
        booking.setStartDate(LocalDate.of(2026, 10, 2));
        booking.setEndDate(LocalDate.of(2026, 10, 4));
        booking.setStatus("P");
        booking.setOwnerMessage(null);
        return booking;
    }

    @Test
    void ownerConfirmsPendingBookingAndSavesMessageAndTimestampBeforeEmail() {
        Booking booking = booking();
        when(bookingRepository.findBookingForDecisionById(4)).thenReturn(Optional.of(booking));
        service.confirmBooking(1, 4, "Palun tagasta tööriist esmaspäeval.");

        assertEquals("C", booking.getStatus());
        assertEquals("Palun tagasta tööriist esmaspäeval.", booking.getOwnerMessage());
        assertNotNull(booking.getUpdatedAt());
        verify(bookingRepository).saveAndFlush(booking);
        verify(decisionMailService).sendBookingConfirmed(booking);
    }

    @Test
    void confirmationAllowsNullMessageAndReturnsNotFoundBeforeAuthorization() {
        Booking booking = booking();
        when(bookingRepository.findBookingForDecisionById(4)).thenReturn(Optional.of(booking));
        service.confirmBooking(1, 4, null);
        assertNull(booking.getOwnerMessage());

        when(bookingRepository.findBookingForDecisionById(404)).thenReturn(Optional.empty());
        PrimaryKeyNotFoundException missing = assertThrows(PrimaryKeyNotFoundException.class,
                () -> service.confirmBooking(99, 404, null));
        assertEquals("Ei leidnud primary keyd 'bookingId' väärtusega: 404", missing.getMessage());
    }

    @Test
    void onlyOwnerCanConfirmPendingBooking() {
        when(bookingRepository.findBookingForDecisionById(4)).thenReturn(Optional.of(booking()));
        ForbiddenException notOwner = assertThrows(ForbiddenException.class,
                () -> service.confirmBooking(3, 4, "sõnum"));
        assertEquals("BOOKING_NOT_OWNER", notOwner.getErrorCode());
        verify(bookingRepository, never()).saveAndFlush(any());
        verifyNoInteractions(decisionMailService);
    }

    @Test
    void confirmedOrRejectedBookingCannotBeConfirmedAgain() {
        Booking booking = booking();
        booking.setStatus("C");
        when(bookingRepository.findBookingForDecisionById(4)).thenReturn(Optional.of(booking));
        ForbiddenException error = assertThrows(ForbiddenException.class,
                () -> service.confirmBooking(1, 4, "sõnum"));
        assertEquals("BOOKING_NOT_PENDING", error.getErrorCode());
        verify(bookingRepository, never()).saveAndFlush(any());
        verifyNoInteractions(decisionMailService);

        booking.setStatus("R");
        ForbiddenException rejected = assertThrows(ForbiddenException.class,
                () -> service.confirmBooking(1, 4, "sõnum"));
        assertEquals("BOOKING_NOT_PENDING", rejected.getErrorCode());
        verifyNoInteractions(decisionMailService);
    }

    @Test
    void confirmationDatabaseFailureUsesContractError() {
        when(bookingRepository.findBookingForDecisionById(4)).thenThrow(new DataAccessResourceFailureException("DB down"));
        InternalServerErrorException error = assertThrows(InternalServerErrorException.class,
                () -> service.confirmBooking(1, 4, "sõnum"));
        assertEquals("INTERNAL_SERVER_ERROR", error.getErrorCode());
        assertEquals("Taotluse kinnitamine ebaõnnestus. Palun proovi hiljem uuesti.", error.getMessage());
        verifyNoInteractions(decisionMailService);
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
