package ee.toolrental.persistence.booking;

import ee.toolrental.controller.booking.dto.BookingCreateRequestDto;
import ee.toolrental.controller.booking.dto.BookingResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {
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
}
