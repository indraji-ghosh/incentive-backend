package org.example.incentivebackend.module.master.party.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.enums.StatusEnum;

@Getter
@Setter
public class PartyRequest {

    @NotBlank(message = "Party name is required")
    @Size(max = 150, message = "Party name cannot exceed 150 characters")
    private String partyName;

    private Long businessHeadId;

    @Size(max = 100, message = "Contact person cannot exceed 100 characters")
    private String contactPerson;

    @Size(max = 20, message = "Contact number cannot exceed 20 characters")
    private String contactNumber;

    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Size(max = 150, message = "Account holder name cannot exceed 150 characters")
    private String accountHolderName;

    @Size(max = 50, message = "Account number cannot exceed 50 characters")
    private String accountNo;

    @Size(max = 20, message = "IFSC code cannot exceed 20 characters")
    private String ifscCode;

    @Size(max = 100, message = "Bank name cannot exceed 100 characters")
    private String bankName;

    @Size(max = 100, message = "Branch name cannot exceed 100 characters")
    private String branchName;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    private StatusEnum partyStatus = StatusEnum.A;
}
