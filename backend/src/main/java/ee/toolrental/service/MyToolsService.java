package ee.toolrental.service;

import ee.toolrental.controller.common.dto.MyToolBookingDto;
import ee.toolrental.controller.common.dto.MyToolCardDto;
import ee.toolrental.controller.mytools.dto.MyToolsResponseDto;
import ee.toolrental.infrastructure.exception.DataNotFoundException;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.booking.Booking;
import ee.toolrental.persistence.booking.BookingMapper;
import ee.toolrental.persistence.booking.BookingRepository;
import ee.toolrental.persistence.profile.Profile;
import ee.toolrental.persistence.profile.ProfileRepository;
import ee.toolrental.persistence.tool.Tool;
import ee.toolrental.persistence.tool.ToolMapper;
import ee.toolrental.persistence.tool.ToolRepository;
import ee.toolrental.persistence.toolimage.ToolImage;
import ee.toolrental.persistence.toolimage.ToolImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MyToolsService {
    private static final String LOAD_FAILED = "Minu tööriistade laadimine ebaõnnestus. Palun proovi hiljem uuesti.";
    private static final String PROFILE_NOT_FOUND = "Kasutaja profiili ei leitud.";
    private static final String USER_BLOCKED = "Sinu konto on blokeeritud.";

    private final AppUserService appUserService;
    private final ProfileRepository profileRepository;
    private final BookingRepository bookingRepository;
    private final ToolRepository toolRepository;
    private final ToolImageRepository toolImageRepository;
    private final BookingMapper bookingMapper;
    private final ToolMapper toolMapper;
    private final Clock applicationClock;

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public MyToolsResponseDto getMyTools(Integer userId) {
        try {
            AppUser appUser = appUserService.getValidAppUserBy(userId);
            if ("B".equals(appUser.getStatus())) {
                throw new ForbiddenException(USER_BLOCKED, "USER_BLOCKED");
            }
            Profile profile = profileRepository.findProfileBy(userId)
                    .orElseThrow(() -> new DataNotFoundException(PROFILE_NOT_FOUND, "PROFILE_NOT_FOUND"));

            LocalDate today = LocalDate.now(applicationClock);
            List<Booking> myRentals = bookingRepository.findCurrentRenterBookingsBy(userId, today);
            List<Booking> incomingRequests = bookingRepository.findPendingOwnerBookingsBy(userId, today);
            List<Booking> outgoingRequests = bookingRepository.findPendingRenterBookingsBy(userId, today);
            List<Tool> availableTools = toolRepository.findAvailableOwnerToolsBy(userId, today);
            List<Booking> rentedOutTools = bookingRepository.findCurrentOwnerBookingsBy(userId, today);

            Set<Integer> toolIds = new HashSet<>();
            addBookingToolIds(toolIds, myRentals);
            addBookingToolIds(toolIds, incomingRequests);
            addBookingToolIds(toolIds, outgoingRequests);
            addBookingToolIds(toolIds, rentedOutTools);
            for (Tool tool : availableTools) toolIds.add(tool.getId());
            Map<Integer, byte[]> mainImageDataByToolId = loadMainImageData(toolIds);

            MyToolsResponseDto response = new MyToolsResponseDto();
            response.setUserId(appUser.getId());
            response.setFirstName(appUser.getFirstName());
            response.setLastName(appUser.getLastName());
            response.setEmail(profile.getEmail());
            response.setPhone(profile.getPhone());
            response.setMyRentals(mapBookingCards(myRentals, mainImageDataByToolId));
            response.setIncomingRequests(mapBookingCards(incomingRequests, mainImageDataByToolId));
            response.setOutgoingRequests(mapBookingCards(outgoingRequests, mainImageDataByToolId));
            response.setAvailableTools(mapToolCards(availableTools, mainImageDataByToolId));
            response.setRentedOutTools(mapBookingCards(rentedOutTools, mainImageDataByToolId));
            return response;
        } catch (ForbiddenException | DataNotFoundException | PrimaryKeyNotFoundException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new InternalServerErrorException(LOAD_FAILED);
        }
    }

    private Map<Integer, byte[]> loadMainImageData(Set<Integer> toolIds) {
        if (toolIds.isEmpty()) return Map.of();
        List<ToolImage> mainImages = toolImageRepository.findMainToolImagesByToolIds(toolIds);
        Map<Integer, byte[]> imageDataByToolId = new HashMap<>();
        for (ToolImage mainImage : mainImages) {
            imageDataByToolId.put(mainImage.getTool().getId(), mainImage.getImageData());
        }
        return imageDataByToolId;
    }

    private void addBookingToolIds(Set<Integer> toolIds, List<Booking> bookings) {
        for (Booking booking : bookings) toolIds.add(booking.getTool().getId());
    }

    private List<MyToolBookingDto> mapBookingCards(List<Booking> bookings, Map<Integer, byte[]> imageDataByToolId) {
        return bookings.stream()
                .map(booking -> bookingMapper.toMyToolBookingDto(
                        booking, imageDataByToolId.get(booking.getTool().getId())))
                .toList();
    }

    private List<MyToolCardDto> mapToolCards(List<Tool> tools, Map<Integer, byte[]> imageDataByToolId) {
        return tools.stream()
                .map(tool -> toolMapper.toMyToolCardDto(tool, imageDataByToolId.get(tool.getId())))
                .toList();
    }
}
