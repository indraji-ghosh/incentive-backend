# Incentive ERP - Audit Log Architecture

This document describes the implemented Audit Log Architecture for the Incentive ERP system, following the robustness requirements.

## 1. Architecture Flow

```
REST Controller
  └── Service (Business Operation)
        ├── 1. Read existing state (oldValues)
        ├── 2. Perform business logic & save entity
        ├── 3. Map to Response DTO (newValues)
        └── 4. Call AuditLogService
                 └── AuditLogRepository
                       └── AuditLogEntity (cm_audit_log)
```

We explicitly perform audit logging in the **Service Layer**. This avoids the pitfalls of generic Hibernate interceptors which can accidentally serialize lazy-loaded proxies or leak passwords, and it ensures we capture exact business semantics (like `ALLOCATE`, `REVERSE`, or `APPROVE`).

## 2. Backend Classes

- **`AuditLogEntity`**: Redesigned to support `oldValues`, `newValues`, `businessReference`, `status`, `sourceModule`, and `sourceEntityType`.
- **`AuditAction` (Enum)**: Expanded to include full lifecycle and financial actions (e.g., `CREATE`, `UPDATE`, `APPROVE`, `ALLOCATE`, `ADJUST`).
- **`AuditLogService`**: Centralized service to persist audit events safely.
- **`AuditHelper`**: A Jackson `ObjectMapper` wrapper (`org.example.incentivebackend.common.audit.util.AuditHelper`) used in services to safely serialize DTOs to JSON strings for old/new values.

## 3. Audit Flow (Example: Party Module)

1. **CREATE**: `PartyServiceImpl.create()` maps request to Entity, saves it, maps to Response DTO, and serializes the DTO as `newValues`. `oldValues` is null.
2. **UPDATE**: `PartyServiceImpl.update()` first fetches the old entity, maps to DTO, and serializes as `oldValues`. It then updates, saves, maps to new DTO, and serializes as `newValues`.
3. **DELETE**: `PartyServiceImpl.delete()` fetches the old entity, serializes it as `oldValues`, deletes the entity, and leaves `newValues` as null.

## 4. Financial Flow Audit (Pattern to be followed)

For complex financial flows (e.g., Rake -> Accrued Payable -> Party Payment -> Allocation), the same pattern must be applied. The `businessReference` field should store the RR No, Bill No, or Payment Reference. The `sourceEntityType` and `sourceEntityId` fields must link back to the triggering entity (e.g., a Payable generated from a Rake will have `sourceEntityType = "Rake"` and `sourceEntityId = rakeId`).

## 5. Security & Masking

- All serialization into `oldValues` and `newValues` must use **Response DTOs** (e.g., `PartyResponse`), NEVER raw entities. This inherently protects against password leakage and recursive relationship serialization, as DTOs are already sanitized for external use.

## 6. Testing

- `AuditLogTest` has been updated to reflect the new structure.
- Integration tests for each module should verify that `AuditLogService.createAuditLog` is triggered properly.
