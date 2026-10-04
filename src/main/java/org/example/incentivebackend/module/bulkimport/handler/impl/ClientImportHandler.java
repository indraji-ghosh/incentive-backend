package org.example.incentivebackend.module.bulkimport.handler.impl;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.bulkimport.definition.ColumnDataType;
import org.example.incentivebackend.module.bulkimport.definition.ImportColumnDefinition;
import org.example.incentivebackend.module.bulkimport.definition.MasterImportDefinition;
import org.example.incentivebackend.module.bulkimport.enums.ImportRowStatus;
import org.example.incentivebackend.module.bulkimport.handler.BulkImportHandler;
import org.example.incentivebackend.module.bulkimport.parser.ParsedRow;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.example.incentivebackend.module.master.client.repository.ClientRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ClientImportHandler implements BulkImportHandler<ClientEntity> {

    private final ClientRepository clientRepository;

    @Override
    public MasterImportDefinition getDefinition() {
        return MasterImportDefinition.builder()
                .moduleName("CLIENT")
                .templateName("Client_Master_Template.xlsx")
                .columns(List.of(
                        ImportColumnDefinition.builder()
                                .columnName("CODE")
                                .fieldName("clientShortCode")
                                .required(true)
                                .unique(true)
                                .dataType(ColumnDataType.STRING)
                                .maxLength(50)
                                .build(),
                        ImportColumnDefinition.builder()
                                .columnName("NAME")
                                .fieldName("clientName")
                                .required(true)
                                .unique(false)
                                .dataType(ColumnDataType.STRING)
                                .maxLength(150)
                                .build(),
                        ImportColumnDefinition.builder()
                                .columnName("STATUS")
                                .fieldName("clientStatus")
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
        return "CLIENT";
    }

    @Override
    public void validateBusinessRules(List<ParsedRow> rows) {
        // Collect all codes to avoid N+1 querying
        List<String> codes = rows.stream()
                .map(r -> r.getValues().get("clientShortCode"))
                .filter(c -> c != null && !c.isEmpty())
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        if (codes.isEmpty()) return;

        // Fetch existing codes from DB
        Set<String> existingCodes = clientRepository.findByClientShortCodeIn(codes)
                .stream()
                .map(c -> c.getClientShortCode().toUpperCase())
                .collect(Collectors.toSet());

        for (ParsedRow row : rows) {
            if (row.getStatus() == ImportRowStatus.INVALID) continue;

            String code = row.getValues().get("clientShortCode");
            String status = row.getValues().get("clientStatus");

            // 1. Check DB Duplicate
            if (code != null && existingCodes.contains(code.toUpperCase())) {
                row.addError("Client Code already exists in database: " + code);
            }

            // 2. Validate Status Enum
            if (status != null && !status.isEmpty()) {
                String s = status.toUpperCase();
                if (s.equals("ACTIVE") || s.equals("A")) {
                    row.getValues().put("clientStatus", "A");
                } else if (s.equals("INACTIVE") || s.equals("I")) {
                    row.getValues().put("clientStatus", "I");
                } else {
                    row.addError("Invalid Status: " + status + ". Allowed values: ACTIVE, INACTIVE.");
                }
            }
        }
    }

    @Override
    public void importRows(List<ParsedRow> validRows) {
        List<ClientEntity> entities = new ArrayList<>();
        
        for (ParsedRow row : validRows) {
            ClientEntity entity = new ClientEntity();
            entity.setClientShortCode(row.getValues().get("clientShortCode").toUpperCase());
            entity.setClientName(row.getValues().get("clientName"));
            
            String statusStr = row.getValues().getOrDefault("clientStatus", "A");
            if (statusStr == null || statusStr.isEmpty()) {
                statusStr = "A";
            }
            entity.setClientStatus(StatusEnum.valueOf(statusStr.toUpperCase()));
            
            entities.add(entity);
        }
        
        // Batch save
        clientRepository.saveAll(entities);
    }
}
