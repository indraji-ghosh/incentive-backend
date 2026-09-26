package org.example.incentivebackend.module.auth.dto;

import lombok.*;
import org.example.incentivebackend.module.master.designation.dto.DesignationResponseDTO;
import org.example.incentivebackend.module.master.designationpermission.dto.DesignationPermissionResponseDTO;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EffectivePermissionResponseDTO {
    private DesignationResponseDTO designation;
    private List<DesignationPermissionResponseDTO> permissions;
}
