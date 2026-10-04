package org.example.incentivebackend.module.approval.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class ApprovalWorkflowRequestDTO {

    @NotBlank(message = "Workflow name is required")
    private String workflowName;

    @NotNull(message = "Entity type is required")
    private WorkflowEntityType entityType;

    private String description;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean requireMakerChecker = true;

    @NotEmpty(message = "At least one approval level is required")
    private List<WorkflowLevelRequestDTO> levels;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowLevelRequestDTO {
        @NotNull(message = "Level number is required")
        private Integer levelNumber;

        @NotBlank(message = "Level name is required")
        private String levelName;

        @NotNull(message = "Sequence order is required")
        private Integer sequenceOrder;

        @NotEmpty(message = "At least one approver user is required per level")
        private List<Long> approverUserIds;
    }
}
