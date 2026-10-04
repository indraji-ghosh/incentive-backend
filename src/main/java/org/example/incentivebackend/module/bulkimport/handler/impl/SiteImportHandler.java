package org.example.incentivebackend.module.bulkimport.handler.impl;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.bulkimport.definition.ColumnDataType;
import org.example.incentivebackend.module.bulkimport.definition.ImportColumnDefinition;
import org.example.incentivebackend.module.bulkimport.definition.MasterImportDefinition;
import org.example.incentivebackend.module.bulkimport.enums.ImportRowStatus;
import org.example.incentivebackend.module.bulkimport.handler.BulkImportHandler;
import org.example.incentivebackend.module.bulkimport.parser.ParsedRow;
import org.example.incentivebackend.module.master.site.entity.SiteEntity;
import org.example.incentivebackend.module.master.site.repository.SiteRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SiteImportHandler implements BulkImportHandler<SiteEntity> {

    private final SiteRepository siteRepository;

    @Override
    public MasterImportDefinition getDefinition() {
        return MasterImportDefinition.builder()
                .moduleName("SITE")
                .templateName("Site_Master_Template.xlsx")
                .columns(List.of(
                        ImportColumnDefinition.builder()
                                .columnName("CODE")
                                .fieldName("siteShortCode")
                                .required(true)
                                .unique(true)
                                .dataType(ColumnDataType.STRING)
                                .maxLength(20)
                                .build(),
                        ImportColumnDefinition.builder()
                                .columnName("NAME")
                                .fieldName("siteName")
                                .required(true)
                                .unique(false)
                                .dataType(ColumnDataType.STRING)
                                .maxLength(100)
                                .build(),
                        ImportColumnDefinition.builder()
                                .columnName("STATE")
                                .fieldName("state")
                                .required(false)
                                .unique(false)
                                .dataType(ColumnDataType.STRING)
                                .maxLength(100)
                                .build(),
                        ImportColumnDefinition.builder()
                                .columnName("STATUS")
                                .fieldName("siteStatus")
                                .required(false)
                                .unique(false)
                                .dataType(ColumnDataType.ENUM)
                                .defaultValue("ACTIVE")
                                .build()
                ))
                .build();
    }

    @Override
    public String getModuleName() {
        return "SITE";
    }

    @Override
    public void validateBusinessRules(List<ParsedRow> rows) {
        List<String> codes = rows.stream()
                .map(r -> r.getValues().get("siteShortCode"))
                .filter(c -> c != null && !c.isEmpty())
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        if (codes.isEmpty()) return;

        Set<String> existingCodes = siteRepository.findBySiteShortCodeIn(codes)
                .stream()
                .map(c -> c.getSiteShortCode().toUpperCase())
                .collect(Collectors.toSet());

        for (ParsedRow row : rows) {
            if (row.getStatus() == ImportRowStatus.INVALID) continue;

            String code = row.getValues().get("siteShortCode");
            String status = row.getValues().get("siteStatus");

            if (code != null && existingCodes.contains(code.toUpperCase())) {
                row.addError("Site Code already exists in database: " + code);
            }

            if (status != null && !status.isEmpty()) {
                String s = status.toUpperCase();
                if (s.equals("ACTIVE") || s.equals("A")) {
                    row.getValues().put("siteStatus", "A");
                } else if (s.equals("INACTIVE") || s.equals("I")) {
                    row.getValues().put("siteStatus", "I");
                } else {
                    row.addError("Invalid Status: " + status + ". Allowed values: ACTIVE, INACTIVE.");
                }
            }
        }
    }

    @Override
    public void importRows(List<ParsedRow> validRows) {
        List<SiteEntity> entities = new ArrayList<>();
        
        for (ParsedRow row : validRows) {
            SiteEntity entity = new SiteEntity();
            entity.setSiteShortCode(row.getValues().get("siteShortCode").toUpperCase());
            entity.setSiteName(row.getValues().get("siteName"));
            entity.setState(row.getValues().get("state"));
            
            String statusStr = row.getValues().getOrDefault("siteStatus", "A");
            if (statusStr == null || statusStr.isEmpty()) {
                statusStr = "A";
            }
            entity.setSiteStatus(StatusEnum.valueOf(statusStr.toUpperCase()));
            
            entities.add(entity);
        }
        
        siteRepository.saveAll(entities);
    }
}
