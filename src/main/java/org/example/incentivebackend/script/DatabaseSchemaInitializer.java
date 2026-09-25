package org.example.incentivebackend.script;

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

    @Override
    public void run(String... args) {
        alterColumnNullable("tx_rake_entry", "party_id");
        alterColumnNullable("tx_bill", "party_id");
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
