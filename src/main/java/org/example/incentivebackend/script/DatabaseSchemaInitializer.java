package org.example.incentivebackend.script;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DatabaseSchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        applySchemaPatches();
    }

    @Override
    public void run(String... args) {
        applySchemaPatches();
    }

    private synchronized void applySchemaPatches() {
        alterColumnNullable("tx_rake_entry", "party_id");
        alterColumnNullable("tx_bill", "party_id");

        // Approval permissions columns in mm_designation_page_permission
        addColumnIfNotExists("mm_designation_page_permission", "can_submit", "NUMBER(1) DEFAULT 0 NOT NULL");
        addColumnIfNotExists("mm_designation_page_permission", "can_approve", "NUMBER(1) DEFAULT 0 NOT NULL");
        addColumnIfNotExists("mm_designation_page_permission", "can_reject", "NUMBER(1) DEFAULT 0 NOT NULL");

        // Commission payment workflow & versioning columns
        addColumnIfNotExists("tr_commission_payment", "version", "NUMBER(19) DEFAULT 0 NOT NULL");
        addColumnIfNotExists("tr_commission_payment", "submitted_by", "NUMBER(19)");
        addColumnIfNotExists("tr_commission_payment", "submitted_at", "TIMESTAMP");
        addColumnIfNotExists("tr_commission_payment", "approved_at", "TIMESTAMP");
        addColumnIfNotExists("tr_commission_payment", "rejected_by", "NUMBER(19)");
        addColumnIfNotExists("tr_commission_payment", "rejected_at", "TIMESTAMP");
        addColumnIfNotExists("tr_commission_payment", "rejection_reason", "VARCHAR2(1000)");
    }

    private void addColumnIfNotExists(String tableName, String columnName, String columnDefinition) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_tab_cols WHERE table_name = UPPER(?) AND column_name = UPPER(?)",
                Integer.class,
                tableName,
                columnName
            );
            if (count == null || count == 0) {
                jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD (" + columnName + " " + columnDefinition + ")");
                log.info("Schema fix: successfully added {}.{}", tableName, columnName);
            }
        } catch (Exception e) {
            log.info("Schema fix: column {}.{} check/add skipped or already exists ({})", tableName, columnName, e.getMessage());
        }
    }

    private void alterColumnNullable(String tableName, String columnName) {
        try {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " MODIFY (" + columnName + " NULL)");
            log.info("Schema fix: successfully modified {}.{} to NULL", tableName, columnName);
        } catch (Exception e) {
            log.info("Schema fix: {}.{} might already be nullable or table not created yet ({})", tableName, columnName, e.getMessage());
        }
    }
}
