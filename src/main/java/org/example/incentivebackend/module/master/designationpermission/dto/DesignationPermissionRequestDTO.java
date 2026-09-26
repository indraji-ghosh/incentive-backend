package org.example.incentivebackend.module.master.designationpermission.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DesignationPermissionRequestDTO {
    private Long pageId;
    private Boolean canView;
    private Boolean canCreate;
    private Boolean canEdit;
    private Boolean canDelete;
    private Boolean canExport;
}
