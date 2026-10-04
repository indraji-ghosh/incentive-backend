package org.example.incentivebackend.module.transaction.partyentry.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class PartyEntryResponse {
    private Long id;
    private String partyName;
    private Long businessHeadId;
    private String businessHeadName;
    private Long paymentTypeId;
    private String paymentTypeName;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String remarks;

    
    // Payment Information
    private String accountHolderName;
    private String accountNo;
    private String ifscCode;
    private String bankName;
    private String branchName;
    
    // Collections
    private List<ClientResponseDto> clients;
    private List<SiteResponseDto> sites;
    private List<PartyUnitConfigurationResponse> unitConfigurations;

    // Auditing fields
    private LocalDateTime createdAt;
    private Long createdBy;





    // Simple nested DTOs for referencing Master Data
    @Getter @Setter @Builder
    public static class ClientResponseDto {
        private Long id;
        private String name;
    }

    @Getter @Setter @Builder
    public static class SiteResponseDto {
        private Long id;
        private String name;
    }
}
