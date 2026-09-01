package org.example.incentivebackend.module.transaction.commissionpayment.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class CommissionCalculationServiceImpl implements CommissionCalculationService {

    // Placeholder: This should eventually call the real commission calculation module.
    // For now, we return a mock value or query an existing table if known.
    @Override
    public BigDecimal getTotalCommissionEarned(Long partyId) {
        // Fallback mock logic since the existing logic was not specified in detail.
        // Return 150000 for Party 1 for testing purposes.
        if (partyId == 1L) return new BigDecimal("150000.00");
        if (partyId == 2L) return new BigDecimal("200000.00");
        return new BigDecimal("50000.00"); 
    }
}
