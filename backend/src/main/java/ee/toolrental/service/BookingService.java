package ee.toolrental.service;

import ee.toolrental.controller.booking.dto.BookingApprovalDto;
import ee.toolrental.controller.booking.dto.BookingCreateRequestDto;
import ee.toolrental.infrastructure.mail.BookingDecisionMailService;
import ee.toolrental.controller.booking.dto.BookingResponseDto;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.IncorrectInputException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.infrastructure.mail.BookingRequestMailService;
import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.booking.Booking;
import ee.toolrental.persistence.booking.BookingMapper;
import ee.toolrental.persistence.booking.BookingRepository;
import ee.toolrental.persistence.profile.ProfileRepository;
import ee.toolrental.persistence.tool.Tool;
import ee.toolrental.persistence.tool.ToolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class BookingService {
    private static final ZoneId TALLINN = ZoneId.of("Europe/Tallinn");
    private static final String SAVE_FAILED = "Laenutuse taotluse saatmine ebaõnnestus. Palun proovi hiljem uuesti.";
    private static final String BOOKING_LOAD_FAILED = "Broneeringu laadimine ebaõnnestus. Palun proovi hiljem uuesti.";
    private static final String DECISION_FAILED = "Taotluse kinnitamine ebaõnnestus. Palun proovi hiljem uuesti.";
    private static final String STATUS_PENDING = "P";
    private static final String STATUS_CONFIRMED = "C";

    private final BookingRepository bookingRepository;
    private final ToolRepository toolRepository;
    private final BookingMapper bookingMapper;
    private final AppUserService appUserService;
    private final BookingRequestMailService bookingRequestMailService;
    private final ProfileRepository profileRepository;
    private final BookingDecisionMailService bookingDecisionMailService;

    @Transactional
    public void confirmBooking(Integer ownerId, Integer bookingId, String ownerMessage) {
        try {
            Booking booking = getValidBookingForDecisionBy(bookingId);
            if (!booking.getTool().getOwner().getId().equals(ownerId)) {
                throw new ForbiddenException("Ainult tööriista omanik saab taotlust kinnitada või tagasi lükata", "BOOKING_NOT_OWNER");
            }
            if (!STATUS_PENDING.equals(booking.getStatus())) {
                throw new ForbiddenException("Taotlus on juba kinnitatud või tagasi lükatud", "BOOKING_NOT_PENDING");
            }

            booking.setStatus(STATUS_CONFIRMED);
            booking.setOwnerMessage(ownerMessage);
            booking.setUpdatedAt(Instant.now());
            booking = bookingRepository.saveAndFlush(booking);
            bookingDecisionMailService.sendBookingConfirmed(booking);
        } catch (DataAccessException exception) {
            throw new InternalServerErrorException(DECISION_FAILED);
        }
    }

    private Booking getValidBookingForDecisionBy(Integer bookingId) {
        return bookingRepository.findBookingForDecisionById(bookingId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("bookingId", bookingId));
    }

    @Transactional(readOnly = true)
    public BookingApprovalDto getBooking(Integer actorId, Integer bookingId) {
        try {
            Booking booking = bookingRepository.findBookingWithPartiesById(bookingId)
                    .orElseThrow(() -> new PrimaryKeyNotFoundException("bookingId", bookingId));
            Integer ownerId = booking.getTool().getOwner().getId();
            Integer renterId = booking.getRenter().getId();
            if (!ownerId.equals(actorId) && !renterId.equals(actorId)) {
                throw new ForbiddenException("Sul pole õigust seda broneeringut vaadata", "BOOKING_ACCESS_DENIED");
            }

            BookingApprovalDto response = bookingMapper.toBookingApprovalDto(booking);
            handleContactDetails(response, booking, actorId.equals(ownerId));
            return response;
        } catch (DataAccessException exception) {
            throw new InternalServerErrorException(BOOKING_LOAD_FAILED);
        }
    }

    private void handleContactDetails(BookingApprovalDto response, Booking booking, boolean isOwner) {
        AppUser contact = isOwner ? booking.getRenter() : booking.getTool().getOwner();
        response.setIsOwner(isOwner);
        response.setContactName(contact.getFirstName() + " " + contact.getLastName());
        profileRepository.findProfileByUserId(contact.getId()).ifPresent(profile -> {
            response.setContactEmail(profile.getEmail());
            response.setContactPhone(profile.getPhone());
        });
    }

    @Transactional
    public BookingResponseDto createBooking(Integer actorId, BookingCreateRequestDto request) {
        validateDates(request);
        try {
            Tool tool = getValidToolBy(request.getToolId());
            if (tool.getOwner().getId().equals(actorId)) {
                throw new ForbiddenException("Enda tööriista ei saa laenata", "OWN_TOOL_BOOKING_FORBIDDEN");
            }
            if (!"A".equals(tool.getStatus())) {
                throw new ForbiddenException("Tööriist pole hetkel saadaval", "TOOL_UNAVAILABLE");
            }
            if (bookingRepository.existsActiveBookingOverlapping(tool.getId(), request.getStartDate(), request.getEndDate())) {
                throw new ForbiddenException("Tööriist on valitud perioodil juba broneeritud", "TOOL_ALREADY_BOOKED");
            }

            AppUser renter = appUserService.getValidAppUserBy(actorId);
            Booking booking = bookingMapper.toBooking(request);
            booking.setTool(tool);
            booking.setRenter(renter);
            booking.setStatus("P");
            booking.setGoogleEventId(null);
            Instant now = Instant.now();
            booking.setCreatedAt(now);
            booking.setUpdatedAt(now);
            booking = bookingRepository.saveAndFlush(booking);
            BookingResponseDto response = bookingMapper.toBookingResponseDto(booking);
            bookingRequestMailService.sendBookingRequest(booking);
            return response;
        } catch (DataAccessException exception) {
            throw new InternalServerErrorException(SAVE_FAILED);
        }
    }

    public Tool getValidToolBy(Integer toolId) {
        return toolRepository.findToolForBookingBy(toolId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("toolId", toolId));
    }

    private void validateDates(BookingCreateRequestDto request) {
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        if (startDate == null || endDate == null) {
            throw new IncorrectInputException((startDate == null ? "startDate" : "endDate") + ": on kohustuslik");
        }
        if (startDate.isBefore(LocalDate.now(TALLINN))) {
            throw new IncorrectInputException("startDate: ei tohi olla minevikus");
        }
        if (endDate.isBefore(startDate)) {
            throw new IncorrectInputException("endDate: peab olema startDate'iga samal päeval või hiljem");
        }
    }
}
