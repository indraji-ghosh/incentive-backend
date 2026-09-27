package org.example.incentivebackend.module.dashboard.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public class DashboardRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private String buildDateFilter(String fieldPrefix, LocalDate from, LocalDate to) {
        StringBuilder sb = new StringBuilder();
        if (from != null) {
            sb.append(" AND ").append(fieldPrefix).append(" >= :fromDate ");
        }
        if (to != null) {
            sb.append(" AND ").append(fieldPrefix).append(" <= :toDate ");
        }
        return sb.toString();
    }

    private void setDateParameters(Query query, LocalDate from, LocalDate to) {
        if (from != null) {
            query.setParameter("fromDate", from);
        }
        if (to != null) {
            query.setParameter("toDate", to);
        }
    }

    public Long getTotalRakes(LocalDate from, LocalDate to) {
        String jpql = "SELECT COUNT(r) FROM RakeEntryEntity r WHERE 1=1 " + buildDateFilter("r.workingMonth", from, to);
        Query query = entityManager.createQuery(jpql);
        setDateParameters(query, from, to);
        return (Long) query.getSingleResult();
    }

    public BigDecimal getTotalBilling(LocalDate from, LocalDate to) {
        String jpql = "SELECT SUM(b.billAmount) FROM BillEntity b WHERE 1=1 " + buildDateFilter("b.workingMonth", from, to);
        Query query = entityManager.createQuery(jpql);
        setDateParameters(query, from, to);
        BigDecimal result = (BigDecimal) query.getSingleResult();
        return result != null ? result : BigDecimal.ZERO;
    }

    public BigDecimal getCustomerOutstanding(LocalDate from, LocalDate to) {
        String jpql = "SELECT SUM(b.outstandingAmount) FROM BillEntity b WHERE 1=1 " + buildDateFilter("b.workingMonth", from, to);
        Query query = entityManager.createQuery(jpql);
        setDateParameters(query, from, to);
        BigDecimal result = (BigDecimal) query.getSingleResult();
        return result != null ? result : BigDecimal.ZERO;
    }

    public BigDecimal getPartyOutstanding(LocalDate from, LocalDate to) {
        // From user rule: "This represents currently outstanding party payable amount."
        // We use outstandingAmount from PartyPayableEntity
        String jpql = "SELECT SUM(p.outstandingAmount) FROM PartyPayableEntity p WHERE 1=1 " + buildDateFilter("p.transactionDate", from, to);
        Query query = entityManager.createQuery(jpql);
        setDateParameters(query, from, to);
        BigDecimal result = (BigDecimal) query.getSingleResult();
        return result != null ? result : BigDecimal.ZERO;
    }

    public BigDecimal getPartyPaid(LocalDate from, LocalDate to) {
        String jpql = "SELECT SUM(p.paymentAmount) FROM CommissionPaymentEntity p WHERE p.status != 'ADVANCE' " + buildDateFilter("p.paymentDate", from, to);
        Query query = entityManager.createQuery(jpql);
        setDateParameters(query, from, to);
        BigDecimal result = (BigDecimal) query.getSingleResult();
        return result != null ? result : BigDecimal.ZERO;
    }

    public BigDecimal getPartyAdvance(LocalDate from, LocalDate to) {
        String jpql = "SELECT SUM(p.paymentAmount) FROM CommissionPaymentEntity p WHERE p.status = 'ADVANCE' " + buildDateFilter("p.paymentDate", from, to);
        Query query = entityManager.createQuery(jpql);
        setDateParameters(query, from, to);
        BigDecimal result = (BigDecimal) query.getSingleResult();
        return result != null ? result : BigDecimal.ZERO;
    }
}
