package ee.toolrental.persistence.booking;

import ee.toolrental.controller.booking.dto.BookingCreateRequestDto;
import ee.toolrental.controller.booking.dto.BookingApprovalDto;
import ee.toolrental.controller.booking.dto.BookingResponseDto;
import ee.toolrental.controller.common.dto.MyToolBookingDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(source = "id", target = "bookingId")
    @Mapping(source = "tool.id", target = "toolId")
    @Mapping(source = "tool.name", target = "toolName")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "ownerMessage", target = "ownerMessage")
    @Mapping(target = "isOwner", ignore = true)
    @Mapping(target = "contactName", ignore = true)
    @Mapping(target = "contactEmail", ignore = true)
    @Mapping(target = "contactPhone", ignore = true)
    BookingApprovalDto toBookingApprovalDto(Booking booking);

    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "ownerMessage", target = "ownerMessage")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tool", ignore = true)
    @Mapping(target = "renter", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "googleEventId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Booking toBooking(BookingCreateRequestDto request);

    @Mapping(source = "id", target = "bookingId")
    @Mapping(source = "tool.id", target = "toolId")
    @Mapping(source = "renter.id", target = "renterId")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "ownerMessage", target = "ownerMessage")
    BookingResponseDto toBookingResponseDto(Booking booking);

    @Mapping(target = "toolId", source = "booking.tool.id")
    @Mapping(target = "toolName", source = "booking.tool.name")
    @Mapping(target = "toolStatus", source = "booking.tool.status")
    @Mapping(target = "imageData", expression = "java(imageData == null ? null : java.util.Base64.getEncoder().encodeToString(imageData))")
    @Mapping(target = "bookingId", source = "booking.id")
    @Mapping(target = "startDate", source = "booking.startDate")
    @Mapping(target = "endDate", source = "booking.endDate")
    @Mapping(target = "bookingStatus", source = "booking.status")
    MyToolBookingDto toMyToolBookingDto(Booking booking, byte[] imageData);
}
