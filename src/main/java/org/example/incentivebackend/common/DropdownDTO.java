package org.example.incentivebackend.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DropdownDTO {

    private Long value;

    private String label;

    private String siteType;

    public DropdownDTO(Long value, String label) {
        this.value = value;
        this.label = label;
    }
}