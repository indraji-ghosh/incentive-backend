package org.example.incentivebackend.module.bulkimport.handler;

import org.example.incentivebackend.module.bulkimport.definition.MasterImportDefinition;
import org.example.incentivebackend.module.bulkimport.parser.ParsedRow;

import java.util.List;

public interface BulkImportHandler<T> {

    /**
     * Returns the import definition for this specific module.
     */
    MasterImportDefinition getDefinition();

    /**
     * Module name that this handler is responsible for (e.g. "CLIENT")
     */
    String getModuleName();

    /**
     * Validate business logic on parsed rows (e.g. DB duplicates, custom constraints).
     * The row's error list should be updated if errors are found.
     */
    void validateBusinessRules(List<ParsedRow> rows);

    /**
     * Commit the valid rows to the database.
     * Only rows with status VALID will be passed here.
     */
    void importRows(List<ParsedRow> validRows);
}
