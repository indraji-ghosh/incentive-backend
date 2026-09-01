package org.example.incentivebackend.module.transaction.rakeEntry.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.enums.StatusEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class RakeEntryResponse {

    private Long rakeEntryId;
    private LocalDate workingMonth;

    private Long clientId;
    private String clientName;
    private String clientShortCode;

    private Long partyId;
    private String partyName;

    private String remarks;
    private StatusEnum rakeStatus;

    private List<RakeAnnexureResponse> annexures;

    private LocalDateTime createdAt;
    private String createdBy;
    private Integer modNo;
    private String modBy;
    private LocalDateTime modAt;
}
