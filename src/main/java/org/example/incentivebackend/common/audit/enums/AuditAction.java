package org.example.incentivebackend.common.audit.enums;

public enum AuditAction {
    // CRUD
    CREATE,
    UPDATE,
    DELETE,
    SOFT_DELETE,
    RESTORE,

    // Status
    ACTIVATE,
    DEACTIVATE,

    // Workflow
    SUBMIT,
    APPROVE,
    REJECT,
    CANCEL,
    REVERSE,

    // Financial / Operational
    ALLOCATE,
    DEALLOCATE,
    ADJUST,
    GENERATE,
    REGENERATE,

    // Relationship
    ASSIGN,
    UNASSIGN,

    // Security
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGOUT,
    PASSWORD_RESET,
    PERMISSION_GRANTED,
    PERMISSION_REVOKED,
    PERMISSION_UPDATED
}