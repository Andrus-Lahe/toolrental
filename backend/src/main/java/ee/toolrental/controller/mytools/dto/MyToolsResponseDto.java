package ee.toolrental.controller.mytools.dto;

import ee.toolrental.controller.common.dto.MyToolBookingDto;
import ee.toolrental.controller.common.dto.MyToolCardDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MyToolsResponseDto {
    private Integer userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private List<MyToolBookingDto> myRentals = new ArrayList<>();
    private List<MyToolBookingDto> incomingRequests = new ArrayList<>();
    private List<MyToolBookingDto> outgoingRequests = new ArrayList<>();
    private List<MyToolCardDto> availableTools = new ArrayList<>();
    private List<MyToolBookingDto> rentedOutTools = new ArrayList<>();
}
