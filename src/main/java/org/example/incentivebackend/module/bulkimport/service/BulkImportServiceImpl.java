package org.example.incentivebackend.module.bulkimport.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.module.bulkimport.entity.ImportRowResultEntity;
import org.example.incentivebackend.module.bulkimport.entity.ImportSessionEntity;
import org.example.incentivebackend.module.bulkimport.enums.ImportRowStatus;
import org.example.incentivebackend.module.bulkimport.enums.ImportSessionStatus;
import org.example.incentivebackend.module.bulkimport.handler.BulkImportHandler;
import org.example.incentivebackend.module.bulkimport.parser.ExcelParserUtil;
import org.example.incentivebackend.module.bulkimport.parser.ParsedRow;
import org.example.incentivebackend.module.bulkimport.repository.ImportRowResultRepository;
import org.example.incentivebackend.module.bulkimport.repository.ImportSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BulkImportServiceImpl implements BulkImportService {

    private final List<BulkImportHandler<?>> handlers;
    private final ImportSessionRepository sessionRepository;
    private final ImportRowResultRepository rowResultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public String validateFile(String moduleName, MultipartFile file) throws Exception {
        BulkImportHandler<?> handler = getHandler(moduleName);

        // 1. Parse the Excel file and perform base validations
        List<ParsedRow> rows = ExcelParserUtil.parse(file, handler.getDefinition());

        // 2. Perform DB / Business validations
        handler.validateBusinessRules(rows);

        // 3. Calculate statistics
        int total = rows.size();
        int valid = (int) rows.stream().filter(r -> r.getStatus() == ImportRowStatus.VALID).count();
        int invalid = total - valid;

        // 4. Create Session
        ImportSessionEntity session = new ImportSessionEntity();
        String importId = "IMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        session.setImportId(importId);
        session.setModuleName(moduleName);
        session.setFileName(file.getOriginalFilename());
        session.setStatus(ImportSessionStatus.VALIDATED);
        session.setTotalRows(total);
        session.setValidRows(valid);
        session.setInvalidRows(invalid);
        sessionRepository.save(session);

        // 5. Save Row Results
        List<ImportRowResultEntity> rowEntities = new ArrayList<>();
        for (ParsedRow row : rows) {
            ImportRowResultEntity rowEntity = new ImportRowResultEntity();
            rowEntity.setImportSession(session);
            rowEntity.setRowNumber(row.getRowNumber());
            rowEntity.setStatus(row.getStatus());
            rowEntity.setRawData(objectMapper.writeValueAsString(row.getValues()));
            if (!row.getErrors().isEmpty()) {
                rowEntity.setErrors(objectMapper.writeValueAsString(row.getErrors()));
            }
            rowEntities.add(rowEntity);
        }
        rowResultRepository.saveAll(rowEntities);

        return importId;
    }

    @Override
    @Transactional
    public void confirmImport(String importId) throws Exception {
        ImportSessionEntity session = sessionRepository.findByImportId(importId)
                .orElseThrow(() -> new RuntimeException("Import Session not found"));

        if (session.getStatus() != ImportSessionStatus.VALIDATED) {
            throw new RuntimeException("Import is not in VALIDATED state or already processed.");
        }

        BulkImportHandler<?> handler = getHandler(session.getModuleName());
        List<ImportRowResultEntity> dbRows = rowResultRepository.findByImportSessionIdOrderByRowNumberAsc(session.getId());

        // Reconstruct ParsedRows for valid rows only
        List<ParsedRow> candidateRows = new ArrayList<>();
        for (ImportRowResultEntity dbRow : dbRows) {
            if (dbRow.getStatus() == ImportRowStatus.VALID) {
                ParsedRow pr = new ParsedRow();
                pr.setRowNumber(dbRow.getRowNumber());
                pr.setStatus(ImportRowStatus.VALID);
                pr.setValues(objectMapper.readValue(dbRow.getRawData(), new TypeReference<Map<String, String>>() {}));
                candidateRows.add(pr);
            }
        }

        // 6. Re-validate business rules (Concurrecy safety)
        handler.validateBusinessRules(candidateRows);

        List<ParsedRow> validToImport = new ArrayList<>();
        int freshlyInvalidated = 0;
        
        for (ParsedRow candidate : candidateRows) {
            if (candidate.getStatus() == ImportRowStatus.VALID) {
                validToImport.add(candidate);
            } else {
                freshlyInvalidated++;
                // Update the dbRow status to INVALID since it failed re-validation
                ImportRowResultEntity dbRow = dbRows.stream()
                        .filter(r -> r.getRowNumber() == candidate.getRowNumber())
                        .findFirst().orElseThrow();
                dbRow.setStatus(ImportRowStatus.INVALID);
                dbRow.setErrors(objectMapper.writeValueAsString(candidate.getErrors()));
            }
        }

        // 7. Execute the import
        if (!validToImport.isEmpty()) {
            handler.importRows(validToImport);
            // Mark these as imported
            for (ParsedRow importedRow : validToImport) {
                ImportRowResultEntity dbRow = dbRows.stream()
                        .filter(r -> r.getRowNumber() == importedRow.getRowNumber())
                        .findFirst().orElseThrow();
                dbRow.setStatus(ImportRowStatus.IMPORTED);
            }
        }

        // 8. Update Session Status
        session.setImportedRows(validToImport.size());
        session.setFailedRows(session.getInvalidRows() + freshlyInvalidated);
        session.setStatus(session.getFailedRows() > 0 ? ImportSessionStatus.PARTIALLY_COMPLETED : ImportSessionStatus.COMPLETED);
        
        rowResultRepository.saveAll(dbRows);
        sessionRepository.save(session);
    }

    private BulkImportHandler<?> getHandler(String moduleName) {
        return handlers.stream()
                .filter(h -> h.getModuleName().equalsIgnoreCase(moduleName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No bulk import handler found for module: " + moduleName));
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getPreviewData(String importId) {
        ImportSessionEntity session = sessionRepository.findByImportId(importId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + importId));
                
        List<ImportRowResultEntity> rows = rowResultRepository.findByImportSessionIdOrderByRowNumberAsc(session.getId());
        
        List<Map<String, Object>> mappedRows = new ArrayList<>();
        for (ImportRowResultEntity row : rows) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("rowNumber", row.getRowNumber());
            map.put("status", row.getStatus());
            try {
                if (row.getRawData() != null) {
                    map.put("data", objectMapper.readValue(row.getRawData(), new TypeReference<Map<String, String>>() {}));
                }
                if (row.getErrors() != null) {
                    map.put("errors", objectMapper.readValue(row.getErrors(), new TypeReference<List<String>>() {}));
                } else {
                    map.put("errors", new ArrayList<>());
                }
            } catch (Exception e) {}
            mappedRows.add(map);
        }
        
        return java.util.Map.of(
            "importId", session.getImportId(),
            "total", session.getTotalRows(),
            "valid", session.getValidRows(),
            "invalid", session.getInvalidRows(),
            "rows", mappedRows
        );
    }
}
