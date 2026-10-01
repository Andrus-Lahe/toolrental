package ee.toolrental.infrastructure.mail;

import ee.toolrental.persistence.booking.Booking;
import ee.toolrental.persistence.profile.Profile;
import ee.toolrental.persistence.profile.ProfileRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingRequestMailService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final ProfileRepository profileRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${toolrental.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username:}")
    private String senderEmail;

    public void sendBookingRequest(Booking booking) {
        try {
            Optional<Profile> ownerProfile = profileRepository.findProfileBy(booking.getTool().getOwner().getId());
            if (ownerProfile.isEmpty()) {
                log.warn("Tööriista omanikul {} puudub profiil; broneeringu {} e-kirja ei saadetud",
                        booking.getTool().getOwner().getId(), booking.getId());
                return;
            }
            String renterEmail = profileRepository.findProfileBy(booking.getRenter().getId())
                    .map(Profile::getEmail).orElse(null);

            Context context = new Context();
            context.setVariable("toolName", booking.getTool().getName());
            context.setVariable("bookingId", booking.getId());
            context.setVariable("renterName", booking.getRenter().getFirstName());
            context.setVariable("startDate", DATE_FORMAT.format(booking.getStartDate()));
            context.setVariable("endDate", DATE_FORMAT.format(booking.getEndDate()));
            context.setVariable("bookingUrl", frontendUrl.replaceAll("/$", "") + "/bookings/" + booking.getId());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(ownerProfile.get().getEmail());
            if (renterEmail != null) {
                helper.setReplyTo(renterEmail);
            }
            helper.setSubject("Uus laenutuse taotlus: " + booking.getTool().getName());
            helper.setText(templateEngine.process("email/booking-request", context), true);
            mailSender.send(message);
        } catch (MailException | MessagingException exception) {
            log.error("Broneeringu {} e-kirja saatmine ebaõnnestus", booking.getId(), exception);
        }
    }
}
