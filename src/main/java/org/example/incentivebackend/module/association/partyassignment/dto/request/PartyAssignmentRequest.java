package org.example.incentivebackend.module.association.partyassignment.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.enums.StatusEnum;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PartyAssignmentRequest {

    @NotNull(message = "Party ID is required")
    private Long partyId;

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "Site ID is required")
    private Long siteId;

    private StatusEnum status = StatusEnum.A;

    private List<@Valid PartyServiceConfigurationRequest> serviceConfigurations = new ArrayList<>();
}
