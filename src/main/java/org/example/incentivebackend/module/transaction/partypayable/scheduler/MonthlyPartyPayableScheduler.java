package org.example.incentivebackend.module.transaction.partypayable.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.incentivebackend.module.transaction.partypayable.entity.PartyPayableEntity;
import org.example.incentivebackend.module.transaction.partypayable.service.PartyPayableService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyPartyPayableScheduler {

    private final PartyPayableService partyPayableService;

    /**
     * Executes on the 1st day of every month at midnight (00:00:00).
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void scheduleMonthlyFixedPayables() {
        log.info("Starting scheduled generation of Monthly Fixed Party Payables...");
        List<PartyPayableEntity> payables = partyPayableService.createMonthlyFixedPayables(LocalDate.now());
        log.info("Successfully generated {} Monthly Fixed Party Payables.", payables.size());
    }

    /**
     * Public helper to trigger generation for a specific month date (useful for tests and manual runs).
     */
    public List<PartyPayableEntity> generateForMonth(LocalDate date) {
        return partyPayableService.createMonthlyFixedPayables(date);
    }
}
