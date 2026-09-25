package org.example.incentivebackend.module.association.partyassignment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.incentivebackend.common.enums.StatusEnum;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyAssignmentResponse {

    private Long id;
    private Long partyId;
    private String partyName;
    private Long clientId;
    private String clientName;
    private String clientShortCode;
    private Long siteId;
    private String siteName;
    private String siteShortCode;
    private StatusEnum status;
    private List<PartyServiceConfigurationResponse> serviceConfigurations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
