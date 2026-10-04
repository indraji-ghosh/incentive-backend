package org.example.incentivebackend.module.bulkimport.parser;

import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.module.bulkimport.enums.ImportRowStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ParsedRow {
    private int rowNumber;
    private Map<String, String> values = new HashMap<>(); // fieldName -> raw value
    private List<String> errors = new ArrayList<>();
    private ImportRowStatus status = ImportRowStatus.VALID;

    public void addError(String errorMsg) {
        errors.add(errorMsg);
        status = ImportRowStatus.INVALID;
    }
}
