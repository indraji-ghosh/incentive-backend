package org.example.incentivebackend.module.master.user.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private Long id;
    private String username;
    private String employeeName;
    private String email;
    private String mobileNumber;
    private String role;
    private String designation;
    private Boolean isActive;
}
