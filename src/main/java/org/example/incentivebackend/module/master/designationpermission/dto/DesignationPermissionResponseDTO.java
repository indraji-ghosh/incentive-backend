package org.example.incentivebackend.module.master.designationpermission.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DesignationPermissionResponseDTO {
    private Long pageId;
    private String pageCode;
    private String pageName;
    private Boolean canView;
    private Boolean canCreate;
    private Boolean canEdit;
    private Boolean canDelete;
    private Boolean canExport;
}
