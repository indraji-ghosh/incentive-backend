package org.example.incentivebackend.module.bulkimport.definition;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImportColumnDefinition {
    private String columnName;
    private String fieldName;
    private boolean required;
    private boolean unique;
    private ColumnDataType dataType;
    private Integer maxLength;
    private String defaultValue;
}
