package org.example.incentivebackend.module.transaction.partyentry.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyLookupResponse {
    private Long id;
    private String partyName;
}
