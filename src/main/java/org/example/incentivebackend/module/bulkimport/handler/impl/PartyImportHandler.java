package org.example.incentivebackend.module.bulkimport.handler.impl;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.bulkimport.definition.ColumnDataType;
import org.example.incentivebackend.module.bulkimport.definition.ImportColumnDefinition;
import org.example.incentivebackend.module.bulkimport.definition.MasterImportDefinition;
import org.example.incentivebackend.module.bulkimport.enums.ImportRowStatus;
import org.example.incentivebackend.module.bulkimport.handler.BulkImportHandler;
import org.example.incentivebackend.module.bulkimport.parser.ParsedRow;
import org.example.incentivebackend.module.master.party.entity.PartyEntity;
import org.example.incentivebackend.module.master.party.repository.PartyRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PartyImportHandler implements BulkImportHandler<PartyEntity> {

    private final PartyRepository partyRepository;

    @Override
    public MasterImportDefinition getDefinition() {
        return MasterImportDefinition.builder()
                .moduleName("PARTY")
                .templateName("Party_Master_Template.xlsx")
                .columns(List.of(
                        ImportColumnDefinition.builder()
                                .columnName("PARTY NAME")
                                .fieldName("partyName")
                                .required(true)
                                .unique(true)
                                .dataType(ColumnDataType.STRING)
                                .maxLength(150)
                                .build(),
                        ImportColumnDefinition.builder()
                                .columnName("CONTACT PERSON")
                                .fieldName("contactPerson")
                                .required(true)
                                .unique(false)
                                .dataType(ColumnDataType.STRING)
                                .maxLength(100)
                                .build(),
                        ImportColumnDefinition.builder()
                                .columnName("PHONE NO")
                                .fieldName("contactNumber")
                                .required(true)
                                .unique(false)
                                .dataType(ColumnDataType.STRING)
                                .maxLength(20)
                                .build(),
                        ImportColumnDefinition.builder()
                                .columnName("EMAIL")
                                .fieldName("email")
                                .required(false)
                                .unique(false)
                                .dataType(ColumnDataType.STRING)
                                .maxLength(100)
                                .build(),
                        ImportColumnDefinition.builder()
                                .columnName("BANK NAME")
                                .fieldName("bankName")
                                .required(false)
                                .unique(false)
                                .dataType(ColumnDataType.STRING)
                                .maxLength(100)
                                .build(),
                        ImportColumnDefinition.builder()
                                .columnName("ACCOUNT NO")
                                .fieldName("accountNo")
                                .required(false)
                                .unique(false)
                                .dataType(ColumnDataType.STRING)
                                .maxLength(50)
                                .build(),
                        ImportColumnDefinition.builder()
                                .columnName("IFSC CODE")
                                .fieldName("ifscCode")
                                .required(false)
                                .unique(false)
                                .dataType(ColumnDataType.STRING)
                                .maxLength(20)
                                .build(),
                        ImportColumnDefinition.builder()
                                .columnName("STATUS")
                                .fieldName("partyStatus")
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
        return "PARTY";
    }

    @Override
    public void validateBusinessRules(List<ParsedRow> rows) {
        List<String> names = rows.stream()
                .map(r -> r.getValues().get("partyName"))
                .filter(n -> n != null && !n.isEmpty())
                .collect(Collectors.toList());

        // We assume Party names should be unique for this master
        List<PartyEntity> existingParties = partyRepository.findAll();
        Set<String> existingNames = existingParties.stream()
                .map(PartyEntity::getPartyName)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        for (ParsedRow row : rows) {
            if (row.getStatus() == ImportRowStatus.INVALID) continue;

            String name = row.getValues().get("partyName");
            if (name != null && existingNames.contains(name.toUpperCase())) {
                row.addError("Party Name already exists in database.");
                row.setStatus(ImportRowStatus.INVALID);
            }
            
            String status = row.getValues().get("partyStatus");
            if (status != null && !status.isEmpty()) {
                String upperStatus = status.toUpperCase();
                if (!upperStatus.equals("ACTIVE") && !upperStatus.equals("INACTIVE")) {
                    row.addError("Invalid Status: " + status + ". Allowed values: ACTIVE, INACTIVE.");
                    row.setStatus(ImportRowStatus.INVALID);
                }
            }
        }
    }

    @Override
    public void importRows(List<ParsedRow> validRows) {
        List<PartyEntity> entities = new ArrayList<>();
        for (ParsedRow row : validRows) {
            PartyEntity entity = new PartyEntity();
            entity.setPartyName(row.getValues().get("partyName"));
            entity.setContactPerson(row.getValues().get("contactPerson"));
            entity.setContactNumber(row.getValues().get("contactNumber"));
            
            if (row.getValues().containsKey("email")) {
                entity.setEmail(row.getValues().get("email"));
            }
            if (row.getValues().containsKey("bankName")) {
                entity.setBankName(row.getValues().get("bankName"));
            }
            if (row.getValues().containsKey("accountNo")) {
                entity.setAccountNo(row.getValues().get("accountNo"));
            }
            if (row.getValues().containsKey("ifscCode")) {
                entity.setIfscCode(row.getValues().get("ifscCode"));
            }

            String statusStr = row.getValues().get("partyStatus");
            if (statusStr != null && statusStr.equalsIgnoreCase("INACTIVE")) {
                entity.setPartyStatus(StatusEnum.I);
            } else {
                entity.setPartyStatus(StatusEnum.A);
            }

            entities.add(entity);
        }
        partyRepository.saveAll(entities);
    }

    
}
