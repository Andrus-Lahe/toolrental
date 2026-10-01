package ee.toolrental.service;

import ee.toolrental.infrastructure.mail.BookingDecisionMailService;
import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.booking.Booking;
import ee.toolrental.persistence.profile.Profile;
import ee.toolrental.persistence.profile.ProfileRepository;
import ee.toolrental.persistence.tool.Tool;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BookingDecisionMailServiceTest {
    private ProfileRepository profileRepository;
    private JavaMailSender mailSender;
    private TemplateEngine templateEngine;
    private BookingDecisionMailService service;
    private Booking booking;
    private Profile renterProfile;
    private Profile ownerProfile;
    private MimeMessage message;

    @BeforeEach
    void setUp() {
        profileRepository = mock(ProfileRepository.class);
        mailSender = mock(JavaMailSender.class);
        templateEngine = mock(TemplateEngine.class);
        service = new BookingDecisionMailService(profileRepository, mailSender, templateEngine);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "frontendUrl", "http://localhost:8081/");
        org.springframework.test.util.ReflectionTestUtils.setField(service, "senderEmail", "noreply@example.com");

        AppUser owner = new AppUser();
        owner.setId(1);
        owner.setFirstName("Marko");
        owner.setLastName("Tamm");
        AppUser renter = new AppUser();
        renter.setId(3);
        renterProfile = new Profile();
        renterProfile.setEmail("liis.kask@example.com");
        ownerProfile = new Profile();
        ownerProfile.setEmail("email@Gmail.com");
        ownerProfile.setPhone("56565656");
        Tool tool = new Tool();
        tool.setName("Akutrell");
        tool.setOwner(owner);
        booking = new Booking();
        booking.setId(1);
        booking.setTool(tool);
        booking.setRenter(renter);
        booking.setStartDate(LocalDate.of(2026, 10, 2));
        booking.setEndDate(LocalDate.of(2026, 10, 4));
        booking.setOwnerMessage("Palun helista saabudes");

        message = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);
        when(templateEngine.process(eq("email/booking-confirmed"), any(Context.class))).thenReturn("<html>confirmed</html>");
        when(templateEngine.process(eq("email/booking-rejected"), any(Context.class))).thenReturn("<html>rejected</html>");
        when(profileRepository.findProfileByUserId(3)).thenReturn(Optional.of(renterProfile));
        when(profileRepository.findProfileByUserId(1)).thenReturn(Optional.of(ownerProfile));
    }

    @Test
    void sendsConfirmedEmailToRenterWithOwnerReplyToAndTemplateData() throws Exception {
        service.sendBookingConfirmed(booking);

        assertEquals("Broneering kinnitatud: Akutrell", message.getSubject());
        assertEquals("liis.kask@example.com", message.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        assertEquals("email@Gmail.com", message.getReplyTo()[0].toString());
        assertEquals("noreply@example.com", message.getFrom()[0].toString());
        verify(templateEngine).process(eq("email/booking-confirmed"), argThat(context ->
                "Marko Tamm".equals(context.getVariable("ownerName"))
                        && "56565656".equals(context.getVariable("ownerPhone"))
                        && "Palun helista saabudes".equals(context.getVariable("ownerMessage"))
                        && "http://localhost:8081/bookings/1".equals(context.getVariable("bookingUrl"))));
        verify(mailSender).send(message);
    }

    @Test
    void missingRenterProfileSkipsEmailAndMissingOwnerProfileLeavesReplyToEmpty() throws Exception {
        when(profileRepository.findProfileByUserId(3)).thenReturn(Optional.empty());
        service.sendBookingConfirmed(booking);
        verifyNoInteractions(mailSender);

        when(profileRepository.findProfileByUserId(3)).thenReturn(Optional.of(renterProfile));
        when(profileRepository.findProfileByUserId(1)).thenReturn(Optional.empty());
        service.sendBookingConfirmed(booking);
        assertNull(message.getHeader("Reply-To"));
    }

    @Test
    void mailServerFailureIsSwallowedAfterBookingDecision() {
        doThrow(new MailSendException("SMTP unavailable")).when(mailSender).send(any(MimeMessage.class));
        assertDoesNotThrow(() -> service.sendBookingConfirmed(booking));
    }

    @Test
    void sendsRejectionEmailWithOwnerMessageAndWithoutOwnerContactRows() throws Exception {
        service.sendBookingRejected(booking);

        assertEquals("Broneering tagasi lükatud: Akutrell", message.getSubject());
        assertEquals("liis.kask@example.com", message.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        assertEquals("email@Gmail.com", message.getReplyTo()[0].toString());
        verify(templateEngine).process(eq("email/booking-rejected"), argThat(context ->
                "Marko Tamm".equals(context.getVariable("ownerName"))
                        && "Palun helista saabudes".equals(context.getVariable("ownerMessage"))
                        && "02.10.2026".equals(context.getVariable("startDate"))
                        && "04.10.2026".equals(context.getVariable("endDate"))
                        && context.getVariable("ownerEmail") == null
                        && context.getVariable("ownerPhone") == null));
        verify(mailSender).send(message);
    }
}
