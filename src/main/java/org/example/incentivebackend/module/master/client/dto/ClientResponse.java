package org.example.incentivebackend.module.master.client.dto;


import lombok.Builder;
import lombok.Getter;
import org.example.incentivebackend.common.enums.StatusEnum;

import java.time.LocalDateTime;

@Getter
@Builder
public class ClientResponse {

    private Long clientId;

    private String clientName;

    private String clientShortCode;

    private StatusEnum clientStatus;

    private LocalDateTime createdAt;

    private Long createdBy;

    private LocalDateTime modAt;

    private Long modBy;

    private String appStatus;

    private Long appBy;

    private LocalDateTime appAt;
}