package org.example.incentivebackend.module.dashboard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.incentivebackend.module.dashboard.dto.response.DashboardSummaryResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final org.example.incentivebackend.module.dashboard.repository.DashboardRepository dashboardRepository;

    public DashboardSummaryResponse getDashboardSummary(LocalDate from, LocalDate to, Long clientId, Long siteId) {
        log.info("Fetching dashboard summary from {} to {} for client {} and site {}", from, to, clientId, siteId);
        
        Long totalRakes = dashboardRepository.getTotalRakes(from, to);
        BigDecimal totalBilling = dashboardRepository.getTotalBilling(from, to);
        BigDecimal partyOutstanding = dashboardRepository.getPartyOutstanding(from, to);
        BigDecimal partyPaid = dashboardRepository.getPartyPaid(from, to);
        BigDecimal customerOutstanding = dashboardRepository.getCustomerOutstanding(from, to);
        BigDecimal partyAdvance = dashboardRepository.getPartyAdvance(from, to);

        return DashboardSummaryResponse.builder()
                .kpis(DashboardSummaryResponse.Kpis.builder()
                        .totalRakes(totalRakes)
                        .totalBilling(totalBilling)
                        .partyOutstanding(partyOutstanding)
                        .partyPaid(partyPaid)
                        .customerOutstanding(customerOutstanding)
                        .partyAdvance(partyAdvance)
                        .build())
                .operations(DashboardSummaryResponse.Operations.builder()
                        .covered(0L)
                        .uncovered(0L)
                        .doorPasting(0L)
                        .pending(0L)
                        .build())
                .payables(DashboardSummaryResponse.Payables.builder()
                        .rakeBased(BigDecimal.ZERO)
                        .billBased(BigDecimal.ZERO)
                        .fixed(BigDecimal.ZERO)
                        .build())
                .recentActivities(new ArrayList<>())
                .topParties(new ArrayList<>())
                .topClients(new ArrayList<>())
                .sitePerformance(new ArrayList<>())
                .charts(DashboardSummaryResponse.Charts.builder()
                        .rakeActivity(new ArrayList<>())
                        .billingOverview(new ArrayList<>())
                        .paymentBreakdown(new ArrayList<>())
                        .build())
                .build();
    }
}
