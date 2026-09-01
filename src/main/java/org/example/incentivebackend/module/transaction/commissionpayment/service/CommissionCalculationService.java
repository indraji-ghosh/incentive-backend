package org.example.incentivebackend.module.transaction.commissionpayment.service;

import java.math.BigDecimal;

public interface CommissionCalculationService {
    BigDecimal getTotalCommissionEarned(Long partyId);
}
