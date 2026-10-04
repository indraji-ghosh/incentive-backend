package org.example.incentivebackend.module.bulkimport.definition;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class MasterImportDefinition {
    private String moduleName;
    private String templateName;
    private List<ImportColumnDefinition> columns;
}
