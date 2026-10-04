package org.example.incentivebackend.module.approval.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRejectRequestDTO {

    @NotBlank(message = "Rejection reason is mandatory")
    private String reason;
}
