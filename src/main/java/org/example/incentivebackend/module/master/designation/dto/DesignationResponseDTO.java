package org.example.incentivebackend.module.master.designation.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DesignationResponseDTO {
    private Long id;
    private String designationCode;
    private String designationName;
    private String level;
    private String description;
    private Boolean isActive;
}
