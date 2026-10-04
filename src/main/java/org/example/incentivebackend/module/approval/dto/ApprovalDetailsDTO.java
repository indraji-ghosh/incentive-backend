package org.example.incentivebackend.module.approval.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.incentivebackend.module.approval.enums.ApprovalActionType;
import org.example.incentivebackend.module.approval.enums.ApprovalStatus;
import org.example.incentivebackend.module.approval.enums.WorkflowEntityType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalDetailsDTO {

    private Long instanceId;
    private String workflowName;
    private WorkflowEntityType entityType;
    private Long entityId;
    private ApprovalStatus status;
    private Integer currentLevelNumber;
    private Integer totalLevels;
    private String currentLevelName;
    private Boolean canApprove;
    private Boolean canReject;
    private String approvalDisabledReason;
    private Long submittedById;
    private String submittedByName;
    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;
    private Integer attemptNumber;

    @Builder.Default
    private List<LevelProgressDTO> levels = new ArrayList<>();

    @Builder.Default
    private List<ApprovalActionDTO> history = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LevelProgressDTO {
        private Integer levelNumber;
        private String levelName;
        private ApprovalStatus status;
        private LocalDateTime activatedAt;
        private LocalDateTime completedAt;
        private Long completedById;
        private String completedByName;
        private String remarks;
        @Builder.Default
        private List<ApproverUserDTO> assignedApprovers = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApproverUserDTO {
        private Long userId;
        private String username;
        private String fullName;
        private String designation;
        private Boolean hasApproved;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalActionDTO {
        private Long id;
        private Integer levelNumber;
        private String levelName;
        private ApprovalActionType action;
        private Long actionById;
        private String actionByName;
        private LocalDateTime actionAt;
        private String remarks;
        private Integer attemptNumber;
    }
}
