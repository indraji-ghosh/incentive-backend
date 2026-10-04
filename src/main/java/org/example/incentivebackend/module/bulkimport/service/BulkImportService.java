package org.example.incentivebackend.module.bulkimport.service;

import org.springframework.web.multipart.MultipartFile;

public interface BulkImportService {
    
    /**
     * Upload and validate an excel file for a given module.
     * Returns the generated importId (e.g. IMP-2026-000001).
     */
    String validateFile(String moduleName, MultipartFile file) throws Exception;

    /**
     * Confirm the import session by its importId.
     * Commits valid rows to the database.
     */
    void confirmImport(String importId) throws Exception;
    
    java.util.Map<String, Object> getPreviewData(String importId);
}
