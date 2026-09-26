package org.example.incentivebackend.module.master.user.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDTO {
    private String username;
    private String employeeName;
    private String email;
    private String mobileNumber;
    private String role;
    private String designation;
    private String password;
    private Boolean isActive;
}
