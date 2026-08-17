package org.example.incentivebackend.module.master.businessHead.dto;

import lombok.Data;
import lombok.Getter;
import org.example.incentivebackend.common.enums.SortDirection;
import org.example.incentivebackend.common.enums.StatusEnum;


@Data
@Getter
public class BusinessHeadFilterDTO {

    private int page =0;
    private int limit = 10;
    private String sortBy = "createdAt";
    private SortDirection sortDirection = SortDirection.DESC;

    private String globalSearch;
    private Long searchById ;
    private StatusEnum status;


}
