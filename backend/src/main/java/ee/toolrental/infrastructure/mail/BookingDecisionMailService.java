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
public class BookingDecisionMailService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final ProfileRepository profileRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${toolrental.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username:}")
    private String senderEmail;

    public void sendBookingConfirmed(Booking booking) {
        sendBookingDecision(booking, true);
    }

    public void sendBookingRejected(Booking booking) {
        sendBookingDecision(booking, false);
    }

    private void sendBookingDecision(Booking booking, boolean confirmed) {
        Optional<Profile> renterProfile = profileRepository.findProfileByUserId(booking.getRenter().getId());
        if (renterProfile.isEmpty()) {
            log.warn("Broneeringu {} rentijal puudub profiil; otsuse e-kirja ei saadetud", booking.getId());
            return;
        }

        Optional<Profile> ownerProfile = profileRepository.findProfileByUserId(booking.getTool().getOwner().getId());
        try {
            Context context = new Context();
            context.setVariable("toolName", booking.getTool().getName());
            context.setVariable("bookingId", booking.getId());
            context.setVariable("startDate", DATE_FORMAT.format(booking.getStartDate()));
            context.setVariable("endDate", DATE_FORMAT.format(booking.getEndDate()));
            context.setVariable("ownerName", booking.getTool().getOwner().getFirstName() + " "
                    + booking.getTool().getOwner().getLastName());
            if (confirmed) {
                context.setVariable("ownerEmail", ownerProfile.map(Profile::getEmail).orElse(null));
                context.setVariable("ownerPhone", ownerProfile.map(Profile::getPhone).orElse(null));
            }
            context.setVariable("ownerMessage", booking.getOwnerMessage());
            context.setVariable("bookingUrl", frontendUrl.replaceAll("/$", "") + "/bookings/" + booking.getId());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(renterProfile.get().getEmail());
            if (ownerProfile.isPresent()) {
                helper.setReplyTo(ownerProfile.get().getEmail());
            }
            helper.setSubject("Broneering " + (confirmed ? "kinnitatud: " : "tagasi lükatud: ") + booking.getTool().getName());
            String templateName = confirmed ? "email/booking-confirmed" : "email/booking-rejected";
            helper.setText(templateEngine.process(templateName, context), true);
            mailSender.send(message);
        } catch (MailException | MessagingException exception) {
            log.error("Broneeringu {} otsuse e-kirja saatmine ebaõnnestus", booking.getId(), exception);
        }
    }
}
