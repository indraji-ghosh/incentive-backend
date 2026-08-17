package org.example.incentivebackend.module.transaction.partyentry.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PartyEntryRequest {

    @NotBlank(message = "Party Name is required")
    @Size(max = 150, message = "Party Name cannot exceed 150 characters")
    private String partyName;

    @NotNull(message = "Business Unit is required")
    private Long businessHeadId;

    @NotNull(message = "Payment Type is required")
    private Long paymentTypeId;

    @NotEmpty(message = "At least one client must be selected")
    private List<Long> clientIds;

    @NotEmpty(message = "At least one site must be selected")
    private List<Long> siteIds;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    // Payment Information
    @Size(max = 150)
    private String accountHolderName;

    @Size(max = 50)
    private String accountNo;

    @Size(max = 20)
    private String ifscCode;

    @Size(max = 100)
    private String bankName;

    @Size(max = 100)
    private String branchName;

    @Valid
    @NotEmpty(message = "At least one unit configuration is required")
    private List<PartyUnitConfigurationRequest> unitConfigurations;
}
