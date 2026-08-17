package org.example.incentivebackend.module.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {

    private Long userId;

    private String userName;

    private String fullName;

//    private UserDesignationResponseDTO designation;
//    private String profilePicUrl;
}
