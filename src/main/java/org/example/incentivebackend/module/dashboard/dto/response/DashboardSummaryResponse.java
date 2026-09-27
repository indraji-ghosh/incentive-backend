package org.example.incentivebackend.module.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private Kpis kpis;
    private Operations operations;
    private Payables payables;
    private List<RecentActivity> recentActivities;
    private List<TopParty> topParties;
    private List<TopClient> topClients;
    private List<SitePerformance> sitePerformance;
    private Charts charts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Kpis {
        private Long totalRakes;
        private BigDecimal totalBilling;
        private BigDecimal partyOutstanding;
        private BigDecimal partyPaid;
        private BigDecimal customerOutstanding;
        private BigDecimal partyAdvance;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Operations {
        private Long covered;
        private Long uncovered;
        private Long doorPasting;
        private Long pending;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payables {
        private BigDecimal rakeBased;
        private BigDecimal billBased;
        private BigDecimal fixed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivity {
        private String timestamp;
        private String description;
        private String type; // e.g., RAKE_CREATED, BILL_CREATED
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopParty {
        private String partyName;
        private String clientName;
        private String siteName;
        private BigDecimal outstanding;
        private String lastPaymentDate;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopClient {
        private String clientName;
        private String siteName;
        private BigDecimal billed;
        private BigDecimal received;
        private BigDecimal outstanding;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SitePerformance {
        private String siteName;
        private String clientName;
        private Long rakes;
        private BigDecimal billing;
        private BigDecimal outstanding;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Charts {
        // We will define specific charts later as the requirements say.
        // For now, we return empty objects or basic lists.
        private Object rakeActivity;
        private Object billingOverview;
        private Object paymentBreakdown;
    }
}
