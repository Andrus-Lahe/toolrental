package ee.toolrental.controller.appuser.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDetailResponse implements Serializable {
    private Integer userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
}
