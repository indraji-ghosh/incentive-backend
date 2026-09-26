package org.example.incentivebackend.module.master.designationpermission.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionMatrixRequestDTO {
    private List<DesignationPermissionRequestDTO> permissions;
}
