package org.example.incentivebackend.module.bulkimport.parser;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.incentivebackend.module.bulkimport.definition.ImportColumnDefinition;
import org.example.incentivebackend.module.bulkimport.definition.MasterImportDefinition;

import java.io.ByteArrayOutputStream;

public class TemplateGeneratorUtil {

    public static byte[] generateTemplate(MasterImportDefinition definition) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Template");

            // Create header row
            Row headerRow = sheet.createRow(0);

            // Create styles
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Populate headers
            int colIdx = 0;
            for (ImportColumnDefinition col : definition.getColumns()) {
                Cell cell = headerRow.createCell(colIdx);
                
                String headerTitle = col.getColumnName();
                if (col.isRequired()) {
                    headerTitle += " *";
                }
                cell.setCellValue(headerTitle);
                cell.setCellStyle(headerStyle);
                
                // Adjust column width based on title
                sheet.setColumnWidth(colIdx, (headerTitle.length() + 10) * 256);
                colIdx++;
            }

            // Create a sample instruction row
            Row instructionRow = sheet.createRow(1);
            colIdx = 0;
            for (ImportColumnDefinition col : definition.getColumns()) {
                Cell cell = instructionRow.createCell(colIdx);
                StringBuilder hint = new StringBuilder(col.getDataType().name());
                if (col.getMaxLength() != null) {
                    hint.append(" (Max: ").append(col.getMaxLength()).append(")");
                }
                if (col.getDefaultValue() != null) {
                    hint.append(" [Def: ").append(col.getDefaultValue()).append("]");
                }
                cell.setCellValue(hint.toString());
                
                CellStyle hintStyle = workbook.createCellStyle();
                Font hintFont = workbook.createFont();
                hintFont.setItalic(true);
                hintFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
                hintStyle.setFont(hintFont);
                cell.setCellStyle(hintStyle);
                
                colIdx++;
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
