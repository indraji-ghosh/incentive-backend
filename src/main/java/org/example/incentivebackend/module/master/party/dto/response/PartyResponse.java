package org.example.incentivebackend.module.master.party.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.enums.StatusEnum;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PartyResponse {

    private Long id;
    private String partyName;
    private Long businessHeadId;
    private String businessHeadName;
    private String contactPerson;
    private String contactNumber;
    private String email;
    private String accountHolderName;
    private String accountNo;
    private String ifscCode;
    private String bankName;
    private String branchName;
    private String remarks;
    private StatusEnum partyStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
