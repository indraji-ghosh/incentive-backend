package org.example.incentivebackend.module.bulkimport.controller;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.bulkimport.definition.MasterImportDefinition;
import org.example.incentivebackend.module.bulkimport.handler.BulkImportHandler;
import org.example.incentivebackend.module.bulkimport.parser.TemplateGeneratorUtil;
import org.example.incentivebackend.module.bulkimport.service.BulkImportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/master/bulk-import")
@RequiredArgsConstructor
public class BulkImportController {

    private final BulkImportService bulkImportService;
    private final List<BulkImportHandler<?>> handlers;

    @GetMapping("/template/{moduleName}")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable String moduleName) {
        try {
            BulkImportHandler<?> handler = handlers.stream()
                    .filter(h -> h.getModuleName().equalsIgnoreCase(moduleName))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No handler found for module: " + moduleName));

            MasterImportDefinition definition = handler.getDefinition();
            byte[] fileContent = TemplateGeneratorUtil.generateTemplate(definition);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", definition.getTemplateName());

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{moduleName}/validate")
    public ResponseEntity<?> validateFile(@PathVariable String moduleName, @RequestParam("file") MultipartFile file) {
        try {
            String importId = bulkImportService.validateFile(moduleName, file);
            return ResponseBuilder.created("File validated successfully", Map.of("importId", importId));
        } catch (Exception e) {
            return ResponseBuilder.error("Validation failed: " + e.getMessage());
        }
    }

    @GetMapping("/preview/{importId}")
    public ResponseEntity<?> getPreview(@PathVariable String importId) {
        try {
            Map<String, Object> previewData = bulkImportService.getPreviewData(importId);
            return ResponseBuilder.fetched("Preview data", previewData);
        } catch (Exception e) {
            return ResponseBuilder.error("Failed to load preview: " + e.getMessage());
        }
    }

    @PostMapping("/{moduleName}/confirm")
    public ResponseEntity<?> confirmImport(@PathVariable String moduleName, @RequestBody Map<String, String> payload) {
        try {
            String importId = payload.get("importId");
            if (importId == null || importId.isEmpty()) {
                throw new RuntimeException("ImportId is required.");
            }
            bulkImportService.confirmImport(importId);
            return ResponseBuilder.updated("Import Confirmed", Map.of("importId", importId));
        } catch (Exception e) {
            return ResponseBuilder.error("Import failed: " + e.getMessage());
        }
    }
}
