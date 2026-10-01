package ee.toolrental.controller.booking;

import ee.toolrental.controller.booking.dto.BookingResponseDto;
import ee.toolrental.controller.booking.dto.BookingApprovalDto;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.infrastructure.security.AppUserOidcService;
import ee.toolrental.infrastructure.security.AppUserPrincipal;
import ee.toolrental.infrastructure.security.SecurityConfig;
import ee.toolrental.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import({SecurityConfig.class, BookingValidationConfig.class})
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-client-secret"})
class BookingControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean BookingService bookingService;
    @MockitoBean AppUserOidcService appUserOidcService;

    @Test
    void ownerCanConfirmAndAnonymousCannot() throws Exception {
        mockMvc.perform(patch("/api/bookings/4/confirm").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerMessage\":\"Palun helista enne tulekut\"}")
                        .with(loggedInUser(1, "customer")))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
        verify(bookingService).confirmBooking(1, 4, "Palun helista enne tulekut");

        mockMvc.perform(patch("/api/bookings/4/confirm").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerMessage\":null}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void confirmationValidatesMessageAndReportsBookingErrors() throws Exception {
        mockMvc.perform(patch("/api/bookings/4/confirm").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerMessage\":\"" + "x".repeat(501) + "\"}")
                        .with(loggedInUser(1, "customer")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"))
                .andExpect(jsonPath("$.message").value("ownerMessage: Sõnum võib olla kuni 500 märki"));
        verifyNoInteractions(bookingService);

        doThrow(new ForbiddenException("Ainult tööriista omanik saab taotlust kinnitada või tagasi lükata", "BOOKING_NOT_OWNER"))
                .when(bookingService).confirmBooking(3, 4, null);
        mockMvc.perform(patch("/api/bookings/4/confirm").contentType(MediaType.APPLICATION_JSON)
                        .content("{}").with(loggedInUser(3, "customer")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("BOOKING_NOT_OWNER"));

        reset(bookingService);
        doThrow(new ForbiddenException("Taotlus on juba kinnitatud või tagasi lükatud", "BOOKING_NOT_PENDING"))
                .when(bookingService).confirmBooking(1, 4, null);
        mockMvc.perform(patch("/api/bookings/4/confirm").contentType(MediaType.APPLICATION_JSON)
                        .content("{}").with(loggedInUser(1, "customer")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("BOOKING_NOT_PENDING"));

        reset(bookingService);
        doThrow(new PrimaryKeyNotFoundException("bookingId", 404))
                .when(bookingService).confirmBooking(1, 404, null);
        mockMvc.perform(patch("/api/bookings/404/confirm").contentType(MediaType.APPLICATION_JSON)
                        .content("{}").with(loggedInUser(1, "customer")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRIMARY_KEY_NOT_FOUND"));

        reset(bookingService);
        doThrow(new InternalServerErrorException("Taotluse kinnitamine ebaõnnestus. Palun proovi hiljem uuesti."))
                .when(bookingService).confirmBooking(1, 4, null);
        mockMvc.perform(patch("/api/bookings/4/confirm").contentType(MediaType.APPLICATION_JSON)
                        .content("{}").with(loggedInUser(1, "customer")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Taotluse kinnitamine ebaõnnestus. Palun proovi hiljem uuesti."));
    }

    @Test
    void bookingCanBeReadByAuthenticatedOwnerAndReturnsContactFields() throws Exception {
        BookingApprovalDto response = bookingApprovalResponse();
        when(bookingService.getBooking(1, 4)).thenReturn(response);
        mockMvc.perform(get("/api/bookings/4").with(loggedInUser(1, "customer")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(4))
                .andExpect(jsonPath("$.toolId").value(5))
                .andExpect(jsonPath("$.toolName").value("Muruniiduk"))
                .andExpect(jsonPath("$.isOwner").value(true))
                .andExpect(jsonPath("$.contactName").value("Liis Kask"))
                .andExpect(jsonPath("$.contactEmail").value("liis@example.com"))
                .andExpect(jsonPath("$.contactPhone").value("55501002"))
                .andExpect(jsonPath("$.ownerMessage").doesNotExist());
        verify(bookingService).getBooking(1, 4);
    }

    @Test
    void bookingRequiresLoginAndRejectsMalformedId() throws Exception {
        mockMvc.perform(get("/api/bookings/4")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/bookings/not-an-integer").with(loggedInUser(1, "customer")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"))
                .andExpect(jsonPath("$.message").value("bookingId: peab olema Integer-tüüpi täisarv"));
        verifyNoInteractions(bookingService);
    }

    @Test
    void bookingReadErrorsKeepTheirContract() throws Exception {
        when(bookingService.getBooking(99, 4)).thenThrow(new ForbiddenException(
                "Sul pole õigust seda broneeringut vaadata", "BOOKING_ACCESS_DENIED"));
        mockMvc.perform(get("/api/bookings/4").with(loggedInUser(99, "admin")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("BOOKING_ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").value("Sul pole õigust seda broneeringut vaadata"));

        reset(bookingService);
        when(bookingService.getBooking(1, 404)).thenThrow(new PrimaryKeyNotFoundException("bookingId", 404));
        mockMvc.perform(get("/api/bookings/404").with(loggedInUser(1, "customer")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRIMARY_KEY_NOT_FOUND"));

        reset(bookingService);
        when(bookingService.getBooking(1, 4)).thenThrow(new InternalServerErrorException(
                "Broneeringu laadimine ebaõnnestus. Palun proovi hiljem uuesti."));
        mockMvc.perform(get("/api/bookings/4").with(loggedInUser(1, "customer")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Broneeringu laadimine ebaõnnestus. Palun proovi hiljem uuesti."));
    }

    private static BookingApprovalDto bookingApprovalResponse() {
        BookingApprovalDto response = new BookingApprovalDto();
        response.setBookingId(4);
        response.setToolId(5);
        response.setToolName("Muruniiduk");
        response.setStartDate(LocalDate.of(2026, 10, 2));
        response.setEndDate(LocalDate.of(2026, 10, 4));
        response.setStatus("P");
        response.setOwnerMessage(null);
        response.setIsOwner(true);
        response.setContactName("Liis Kask");
        response.setContactEmail("liis@example.com");
        response.setContactPhone("55501002");
        return response;
    }

    @Test
    void customerGets200AndRenterComesFromSession() throws Exception {
        BookingResponseDto response = new BookingResponseDto();
        response.setBookingId(4);
        response.setToolId(5);
        response.setRenterId(3);
        response.setStartDate(today().plusDays(1));
        response.setEndDate(today().plusDays(2));
        response.setStatus("P");
        response.setOwnerMessage("Tere");
        when(bookingService.createBooking(eq(3), any())).thenReturn(response);

        mockMvc.perform(postBooking(validJson()).with(loggedInUser(3, "customer")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(4))
                .andExpect(jsonPath("$.renterId").value(3))
                .andExpect(jsonPath("$.status").value("P"));
        verify(bookingService).createBooking(eq(3), argThat(request -> request.getToolId() == 5
                && request.getOwnerMessage().equals("Tere")));
    }

    @Test
    void adminCanCreateButAnonymousGets401() throws Exception {
        mockMvc.perform(postBooking(validJson()).with(loggedInUser(1, "admin")))
                .andExpect(status().isOk());
        mockMvc.perform(postBooking(validJson())).andExpect(status().isUnauthorized());
        verify(bookingService).createBooking(eq(1), any());
    }

    @Test
    void missingAndInvalidFieldsReturn400() throws Exception {
        mockMvc.perform(postBooking("{}").with(loggedInUser(3, "customer")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"));
        mockMvc.perform(postBooking(validJson().replace("\"toolId\":5", "\"toolId\":0"))
                        .with(loggedInUser(3, "customer")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("toolId: peab olema positiivne"));
        mockMvc.perform(postBooking(validJson().replace("Tere", "x".repeat(501)))
                        .with(loggedInUser(3, "customer")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"));
        verifyNoInteractions(bookingService);
    }

    @Test
    void malformedDateReturns400WithField() throws Exception {
        mockMvc.perform(postBooking(validJson().replace(today().plusDays(1).toString(), "yesterday"))
                        .with(loggedInUser(3, "customer")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("startDate:")));
    }

    @Test
    void pastStartDateIsRejectedByValidation() throws Exception {
        mockMvc.perform(postBooking(validJson().replace(today().plusDays(1).toString(), today().minusDays(1).toString()))
                        .with(loggedInUser(3, "customer")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("startDate: ei tohi olla minevikus"));
        verifyNoInteractions(bookingService);
    }

    @Test
    void serviceErrorsKeepTheirContract() throws Exception {
        when(bookingService.createBooking(eq(3), any()))
                .thenThrow(new PrimaryKeyNotFoundException("toolId", 123));
        mockMvc.perform(postBooking(validJson()).with(loggedInUser(3, "customer")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRIMARY_KEY_NOT_FOUND"));

        reset(bookingService);
        when(bookingService.createBooking(eq(3), any()))
                .thenThrow(new ForbiddenException("Enda tööriista ei saa laenata", "OWN_TOOL_BOOKING_FORBIDDEN"));
        mockMvc.perform(postBooking(validJson()).with(loggedInUser(3, "customer")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("OWN_TOOL_BOOKING_FORBIDDEN"));

        reset(bookingService);
        when(bookingService.createBooking(eq(3), any()))
                .thenThrow(new InternalServerErrorException("Laenutuse taotluse saatmine ebaõnnestus. Palun proovi hiljem uuesti."));
        mockMvc.perform(postBooking(validJson()).with(loggedInUser(3, "customer")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"));
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder postBooking(String json) {
        return post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content(json);
    }

    private static String validJson() {
        return "{\"toolId\":5,\"startDate\":\"" + today().plusDays(1) + "\",\"endDate\":\""
                + today().plusDays(2) + "\",\"ownerMessage\":\"Tere\",\"renterId\":999,\"status\":\"C\"}";
    }

    private static LocalDate today() {
        return LocalDate.now(ZoneId.of("Europe/Tallinn"));
    }

    private static RequestPostProcessor loggedInUser(int userId, String role) {
        OidcIdToken token = OidcIdToken.withTokenValue("test-token").subject("sub-" + userId)
                .claim("email", "user@example.com").build();
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        AppUserPrincipal principal = new AppUserPrincipal(userId, authorities, token, null);
        return authentication(new OAuth2AuthenticationToken(principal, authorities, "google"));
    }
}
