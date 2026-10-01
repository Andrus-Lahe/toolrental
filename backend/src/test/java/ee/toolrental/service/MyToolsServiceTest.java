package ee.toolrental.service;

import ee.toolrental.controller.mytools.dto.MyToolsResponseDto;
import ee.toolrental.infrastructure.exception.DataNotFoundException;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.booking.Booking;
import ee.toolrental.persistence.booking.BookingMapperImpl;
import ee.toolrental.persistence.booking.BookingRepository;
import ee.toolrental.persistence.profile.Profile;
import ee.toolrental.persistence.profile.ProfileRepository;
import ee.toolrental.persistence.tool.Tool;
import ee.toolrental.persistence.tool.ToolMapperImpl;
import ee.toolrental.persistence.tool.ToolRepository;
import ee.toolrental.persistence.toolimage.ToolImage;
import ee.toolrental.persistence.toolimage.ToolImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

class MyToolsServiceTest {
    private static final Integer USER_ID = 3;
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 25);
    private AppUserService appUserService;
    private ProfileRepository profileRepository;
    private BookingRepository bookingRepository;
    private ToolRepository toolRepository;
    private ToolImageRepository toolImageRepository;
    private MyToolsService service;
    private AppUser appUser;
    private Profile profile;

    @BeforeEach
    void setUp() {
        appUserService = mock(AppUserService.class);
        profileRepository = mock(ProfileRepository.class);
        bookingRepository = mock(BookingRepository.class);
        toolRepository = mock(ToolRepository.class);
        toolImageRepository = mock(ToolImageRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-09-25T10:00:00Z"), ZoneId.of("Europe/Tallinn"));
        service = new MyToolsService(appUserService, profileRepository, bookingRepository, toolRepository,
                toolImageRepository, new BookingMapperImpl(), new ToolMapperImpl(), clock);

        appUser = new AppUser();
        appUser.setId(USER_ID);
        appUser.setFirstName("Liis");
        appUser.setLastName("Kask");
        appUser.setStatus("A");
        profile = new Profile();
        profile.setEmail("liis.kask@example.com");
        profile.setPhone("55501002");

        when(appUserService.getValidAppUserBy(USER_ID)).thenReturn(appUser);
        when(profileRepository.findProfileBy(USER_ID)).thenReturn(Optional.of(profile));
        when(bookingRepository.findCurrentRenterBookingsBy(USER_ID, TODAY)).thenReturn(List.of());
        when(bookingRepository.findPendingOwnerBookingsBy(USER_ID, TODAY)).thenReturn(List.of());
        when(bookingRepository.findPendingRenterBookingsBy(USER_ID, TODAY)).thenReturn(List.of());
        when(toolRepository.findAvailableOwnerToolsBy(USER_ID, TODAY)).thenReturn(List.of());
        when(bookingRepository.findCurrentOwnerBookingsBy(USER_ID, TODAY)).thenReturn(List.of());
    }

    @Test
    void returnsFiveListsProfileAndBase64MainImage() {
        Tool ownTool = tool(8, USER_ID, "Projektor", "A");
        Tool otherTool = tool(1, 7, "Akutrell", "A");
        Booking outgoing = booking(41, otherTool, USER_ID, "P", TODAY.plusDays(7), TODAY.plusDays(9));
        Booking incoming = booking(42, ownTool, 7, "P", TODAY, TODAY.plusDays(2));
        Booking myRental = booking(43, otherTool, USER_ID, "C", TODAY, TODAY);
        Booking rentedOut = booking(44, ownTool, 7, "C", TODAY, TODAY);
        ToolImage mainImage = new ToolImage();
        mainImage.setTool(ownTool);
        mainImage.setImageData(new byte[]{0, 1, 2, (byte) 255});
        mainImage.setMain(true);

        when(bookingRepository.findCurrentRenterBookingsBy(USER_ID, TODAY)).thenReturn(List.of(myRental));
        when(bookingRepository.findPendingOwnerBookingsBy(USER_ID, TODAY)).thenReturn(List.of(incoming));
        when(bookingRepository.findPendingRenterBookingsBy(USER_ID, TODAY)).thenReturn(List.of(outgoing));
        when(toolRepository.findAvailableOwnerToolsBy(USER_ID, TODAY)).thenReturn(List.of(ownTool));
        when(bookingRepository.findCurrentOwnerBookingsBy(USER_ID, TODAY)).thenReturn(List.of(rentedOut));
        when(toolImageRepository.findMainToolImagesByToolIds(anyCollection())).thenReturn(List.of(mainImage));

        MyToolsResponseDto response = service.getMyTools(USER_ID);

        assertEquals(USER_ID, response.getUserId());
        assertEquals("Liis", response.getFirstName());
        assertEquals("liis.kask@example.com", response.getEmail());
        assertEquals("55501002", response.getPhone());
        assertEquals(1, response.getMyRentals().size());
        assertEquals(43, response.getMyRentals().getFirst().getBookingId());
        assertNull(response.getMyRentals().getFirst().getImageData());
        assertEquals("P", response.getIncomingRequests().getFirst().getBookingStatus());
        assertEquals(41, response.getOutgoingRequests().getFirst().getBookingId());
        assertEquals("AAEC/w==", response.getAvailableTools().getFirst().getImageData());
        assertEquals(44, response.getRentedOutTools().getFirst().getBookingId());
        verify(bookingRepository).findCurrentRenterBookingsBy(USER_ID, TODAY);
        verify(bookingRepository).findPendingOwnerBookingsBy(USER_ID, TODAY);
        verify(bookingRepository).findPendingRenterBookingsBy(USER_ID, TODAY);
        verify(bookingRepository).findCurrentOwnerBookingsBy(USER_ID, TODAY);
        verify(toolRepository).findAvailableOwnerToolsBy(USER_ID, TODAY);
    }

    @Test
    void emptyDataReturnsAllListsAsEmptyArraysAndSkipsImageQuery() {
        MyToolsResponseDto response = service.getMyTools(USER_ID);

        assertNotNull(response.getMyRentals());
        assertNotNull(response.getIncomingRequests());
        assertNotNull(response.getOutgoingRequests());
        assertNotNull(response.getAvailableTools());
        assertNotNull(response.getRentedOutTools());
        assertTrue(response.getMyRentals().isEmpty());
        verifyNoInteractions(toolImageRepository);
    }

    @Test
    void blockedUserIsRejectedBeforeProfileOrListsAreRead() {
        appUser.setStatus("B");

        ForbiddenException error = assertThrows(ForbiddenException.class, () -> service.getMyTools(USER_ID));

        assertEquals("USER_BLOCKED", error.getErrorCode());
        verifyNoInteractions(profileRepository, bookingRepository, toolRepository, toolImageRepository);
    }

    @Test
    void missingProfileReturnsContract404() {
        when(profileRepository.findProfileBy(USER_ID)).thenReturn(Optional.empty());

        DataNotFoundException error = assertThrows(DataNotFoundException.class, () -> service.getMyTools(USER_ID));

        assertEquals("PROFILE_NOT_FOUND", error.getErrorCode());
        verifyNoInteractions(bookingRepository, toolRepository, toolImageRepository);
    }

    @Test
    void databaseFailureReturnsGenericTaskError() {
        when(bookingRepository.findPendingOwnerBookingsBy(USER_ID, TODAY))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));

        InternalServerErrorException error = assertThrows(InternalServerErrorException.class,
                () -> service.getMyTools(USER_ID));

        assertEquals("INTERNAL_SERVER_ERROR", error.getErrorCode());
        assertEquals("Minu tööriistade laadimine ebaõnnestus. Palun proovi hiljem uuesti.", error.getMessage());
    }

    private Tool tool(Integer id, Integer ownerId, String name, String status) {
        AppUser owner = new AppUser();
        owner.setId(ownerId);
        Tool tool = new Tool();
        tool.setId(id);
        tool.setOwner(owner);
        tool.setName(name);
        tool.setStatus(status);
        return tool;
    }

    private Booking booking(Integer id, Tool tool, Integer renterId, String status,
                            LocalDate startDate, LocalDate endDate) {
        AppUser renter = new AppUser();
        renter.setId(renterId);
        Booking booking = new Booking();
        booking.setId(id);
        booking.setTool(tool);
        booking.setRenter(renter);
        booking.setStatus(status);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        return booking;
    }
}
