package org.example.incentivebackend.module.master.paymenttype.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentTypeResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String appStatus;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime modAt;
    private Long modBy;
    private LocalDateTime appAt;
    private Long appBy;
}
