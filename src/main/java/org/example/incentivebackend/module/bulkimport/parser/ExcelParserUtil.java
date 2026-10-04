package org.example.incentivebackend.module.bulkimport.parser;

import org.apache.poi.ss.usermodel.*;
import org.example.incentivebackend.module.bulkimport.definition.ImportColumnDefinition;
import org.example.incentivebackend.module.bulkimport.definition.MasterImportDefinition;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

public class ExcelParserUtil {

    public static List<ParsedRow> parse(MultipartFile file, MasterImportDefinition definition) throws Exception {
        List<ParsedRow> parsedRows = new ArrayList<>();
        
        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Map header to column index
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new RuntimeException("Empty Excel file or missing headers");
            }

            Map<String, Integer> columnNameToIndex = new HashMap<>();
            for (Cell cell : headerRow) {
                String headerName = getCellValueAsString(cell).trim().toUpperCase().replace("*", "").trim();
                columnNameToIndex.put(headerName, cell.getColumnIndex());
            }

            // Verify all required columns are present
            for (ImportColumnDefinition col : definition.getColumns()) {
                if (col.isRequired() && !columnNameToIndex.containsKey(col.getColumnName().toUpperCase())) {
                    throw new RuntimeException("Missing required column: " + col.getColumnName());
                }
            }

            // Parse Data Rows
            Set<String> fileLevelUniqueChecks = new HashSet<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                ParsedRow parsedRow = new ParsedRow();
                parsedRow.setRowNumber(i + 1);

                for (ImportColumnDefinition colDef : definition.getColumns()) {
                    Integer colIdx = columnNameToIndex.get(colDef.getColumnName().toUpperCase());
                    String rawValue = "";

                    if (colIdx != null) {
                        Cell cell = row.getCell(colIdx);
                        rawValue = getCellValueAsString(cell).trim();
                    }

                    // Validation: Required
                    if (colDef.isRequired() && rawValue.isEmpty()) {
                        parsedRow.addError(colDef.getColumnName() + " is required.");
                    }

                    // Validation: Max Length
                    if (colDef.getMaxLength() != null && rawValue.length() > colDef.getMaxLength()) {
                        parsedRow.addError(colDef.getColumnName() + " exceeds maximum length of " + colDef.getMaxLength() + ".");
                    }

                    // Validation: File-level Duplicate
                    if (colDef.isUnique() && !rawValue.isEmpty()) {
                        String uniqueKey = colDef.getColumnName() + "_" + rawValue.toUpperCase();
                        if (!fileLevelUniqueChecks.add(uniqueKey)) {
                            parsedRow.addError("Duplicate " + colDef.getColumnName() + " in uploaded file: " + rawValue);
                        }
                    }

                    parsedRow.getValues().put(colDef.getFieldName(), rawValue);
                }
                parsedRows.add(parsedRow);
            }
        }
        return parsedRows;
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    private static boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }
}
