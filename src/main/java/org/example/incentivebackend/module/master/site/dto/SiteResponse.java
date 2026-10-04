package org.example.incentivebackend.module.master.site.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.incentivebackend.common.enums.StatusEnum;

import java.time.LocalDateTime;

@Getter
@Builder
public class SiteResponse {

    private Long siteId;

    private String siteName;

    private String siteShortCode;

    private String state;

    private StatusEnum siteStatus;

    private LocalDateTime createdAt;

    private Long createdBy;










}
