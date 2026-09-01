package org.example.incentivebackend.module.transaction.rakeEntry.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class RakeEntryRequest {

    @NotNull(message = "Working month is required")
    private LocalDate workingMonth;

    @NotNull(message = "Client is required")
    private Long clientId;

    @NotNull(message = "Party is required")
    private Long partyId;

    private String remarks;

    @NotEmpty(message = "At least one rake annexure is required")
    private List<@Valid RakeAnnexureRequest> annexures;
}
