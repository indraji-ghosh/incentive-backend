package org.example.incentivebackend.module.master.site.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.enums.StatusEnum;

@Getter
@Setter
public class SiteFilter {

    private String search;

    private String state;

    private StatusEnum siteStatus;

    private int page = 0;

    private int size = 10;

    private String sortBy = "siteId";

    private String sortDirection = "desc";
}