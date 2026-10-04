package org.example.incentivebackend.module.approval.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.incentivebackend.module.approval.enums.WorkflowEntityType;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalWorkflowResponseDTO {

    private Long id;
    private String workflowName;
    private WorkflowEntityType entityType;
    private String description;
    private Boolean isActive;
    private Boolean requireMakerChecker;
    private List<WorkflowLevelResponseDTO> levels;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowLevelResponseDTO {
        private Long id;
        private Integer levelNumber;
        private String levelName;
        private Integer sequenceOrder;
        private Boolean isActive;
        private List<WorkflowApproverResponseDTO> approvers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowApproverResponseDTO {
        private Long id;
        private Long userId;
        private String username;
        private String fullName;
        private String designation;
    }
}
