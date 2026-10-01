package ee.toolrental.service;

import ee.toolrental.infrastructure.mail.BookingRequestMailService;
import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.booking.Booking;
import ee.toolrental.persistence.profile.Profile;
import ee.toolrental.persistence.profile.ProfileRepository;
import ee.toolrental.persistence.tool.Tool;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BookingRequestMailServiceTest {
    private ProfileRepository profileRepository;
    private JavaMailSender sender;
    private TemplateEngine templateEngine;
    private BookingRequestMailService service;
    private Booking booking;

    @BeforeEach
    void setUp() {
        profileRepository = mock(ProfileRepository.class);
        sender = mock(JavaMailSender.class);
        templateEngine = mock(TemplateEngine.class);
        service = new BookingRequestMailService(profileRepository, sender, templateEngine);
        ReflectionTestUtils.setField(service, "frontendUrl", "http://localhost:8081/");
        ReflectionTestUtils.setField(service, "senderEmail", "app@example.com");

        AppUser owner = new AppUser();
        owner.setId(1);
        AppUser renter = new AppUser();
        renter.setId(3);
        renter.setFirstName("Liis");
        Tool tool = new Tool();
        tool.setId(5);
        tool.setOwner(owner);
        tool.setName("Muruniiduk");
        booking = new Booking();
        booking.setId(4);
        booking.setTool(tool);
        booking.setRenter(renter);
        booking.setStartDate(LocalDate.of(2026, 10, 10));
        booking.setEndDate(LocalDate.of(2026, 10, 12));

        Profile ownerProfile = new Profile();
        ownerProfile.setEmail("email@Gmail.com");
        Profile renterProfile = new Profile();
        renterProfile.setEmail("liis.kask@example.com");
        when(profileRepository.findProfileBy(1)).thenReturn(Optional.of(ownerProfile));
        when(profileRepository.findProfileBy(3)).thenReturn(Optional.of(renterProfile));
        when(sender.createMimeMessage()).thenAnswer(invocation -> new MimeMessage(Session.getInstance(new Properties())));
        when(templateEngine.process(eq("email/booking-request"), any(IContext.class))).thenReturn("<html>booking</html>");
    }

    @Test
    void sendsOwnerMailWithRenterReplyToAndTemplateData() throws Exception {
        service.sendBookingRequest(booking);

        ArgumentCaptor<IContext> context = ArgumentCaptor.forClass(IContext.class);
        verify(templateEngine).process(eq("email/booking-request"), context.capture());
        assertEquals("Muruniiduk", context.getValue().getVariable("toolName"));
        assertEquals(4, context.getValue().getVariable("bookingId"));
        assertEquals("Liis", context.getValue().getVariable("renterName"));
        assertEquals("10.10.2026", context.getValue().getVariable("startDate"));
        assertEquals("12.10.2026", context.getValue().getVariable("endDate"));
        assertEquals("http://localhost:8081/bookings/4", context.getValue().getVariable("bookingUrl"));

        ArgumentCaptor<MimeMessage> message = ArgumentCaptor.forClass(MimeMessage.class);
        verify(sender).send(message.capture());
        assertEquals("email@Gmail.com", message.getValue().getAllRecipients()[0].toString());
        assertEquals("liis.kask@example.com", message.getValue().getReplyTo()[0].toString());
        assertEquals("Uus laenutuse taotlus: Muruniiduk", message.getValue().getSubject());
    }

    @Test
    void ownerWithoutProfileDoesNotSendMail() {
        when(profileRepository.findProfileBy(1)).thenReturn(Optional.empty());
        service.sendBookingRequest(booking);
        verifyNoInteractions(sender, templateEngine);
    }

    @Test
    void mailFailureDoesNotThrow() {
        doThrow(new MailSendException("SMTP down")).when(sender).send(any(MimeMessage.class));
        assertDoesNotThrow(() -> service.sendBookingRequest(booking));
    }
}
