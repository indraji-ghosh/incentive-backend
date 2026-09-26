package org.example.incentivebackend.module.master.page.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponseDTO {
    private Long id;
    private String pageCode;
    private String pageName;
    private String moduleName;
    private String route;
    private String icon;
    private Integer displayOrder;
    private Boolean isActive;
}
