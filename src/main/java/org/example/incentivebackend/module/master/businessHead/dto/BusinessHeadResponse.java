package org.example.incentivebackend.module.master.businessHead.dto;


import lombok.Builder;
import lombok.Getter;
import org.example.incentivebackend.common.enums.StatusEnum;

import java.time.LocalDateTime;

@Getter
@Builder
public class BusinessHeadResponse {

    private Long headId;

    private String headName;

    private String headShortCode;

    private StatusEnum headStatus;

    private LocalDateTime createdAt;

    private Long createdBy;

    private LocalDateTime modAt;

    private Long modBy;

    private String appStatus;

    private Long appBy;

    private LocalDateTime appAt;
}