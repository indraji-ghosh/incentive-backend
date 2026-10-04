# Multi-Level Multi-User Approval System
## Incentive ERP — Architecture, Specification & Integration Manual

---

### Table of Contents
1. [Executive Summary & Core Principles](#1-executive-summary--core-principles)
2. [System Architecture & Design Patterns](#2-system-architecture--design-patterns)
3. [Configuration vs. Runtime Instance Model](#3-configuration-vs-runtime-instance-model)
4. [The "ANY ONE" Approver Rule](#4-the-any-one-approver-rule)
5. [Maker-Checker & Separation of Duties](#5-maker-checker--separation-of-duties)
6. [Complete Approval Lifecycle & State Transitions](#6-complete-approval-lifecycle--state-transitions)
7. [Rejection Handling & Resubmission Flow](#7-rejection-handling--resubmission-flow)
8. [Financial Isolation & Posting Mechanism](#8-financial-isolation--posting-mechanism)
9. [Database Schema & Entity-Relationship Design](#9-database-schema--entity-relationship-design)
10. [Audit Logging & Concurrency Control](#10-audit-logging--concurrency-control)
11. [Role-Based Access Control (RBAC) & Designation Permissions](#11-role-based-access-control-rbac--designation-permissions)
12. [Seed Data & Test Personas](#12-seed-data--test-personas)
13. [Step-by-Step Testing & Verification Guide](#13-step-by-step-testing--verification-guide)
14. [Postman Workflow Scenarios](#14-postman-workflow-scenarios)
15. [Verification SQL Queries](#15-verification-sql-queries)
16. [REST API Reference](#16-rest-api-reference)
17. [Frontend UI Components & User Experience](#17-frontend-ui-components--user-experience)

---

### 1. Executive Summary & Core Principles

The Multi-Level Multi-User Approval System is a generic, enterprise-grade approval framework engineered for Incentive ERP. Designed to govern sensitive operational and financial workflows, the system enforces stringent maker-checker internal controls, deterministic sequential level progression, and immutable auditability.

**Party Payment** is the flagship module onboarded to this approval engine.

#### Foundational System Guarantees:
1. **Multi-Level Sequential Routing**: A workflow progresses through distinct sequential tiers ($L_1 \rightarrow L_2 \rightarrow \dots \rightarrow L_n$). No level $k+1$ can act while level $k$ is pending.
2. **Multi-User Level Membership**: Each level is staffed by multiple authorized approvers.
3. **The "ANY ONE" Consensus Rule**: Approval by **any single authorized approver** at the active level immediately completes that level and advances the workflow to the next sequential tier. Other approvers at that same level can no longer act.
4. **Maker-Checker Enforcement**: The user who created or submitted the document is strictly prohibited from approving or rejecting their own submission, regardless of designations or level assignments.
5. **No Early / Out-of-Order Approval**: Users assigned to future levels cannot approve or reject before their level becomes active.
6. **Mandatory Reason on Rejection**: Workflow termination requires a non-empty rejection justification, resetting the document for correction.
7. **Immutable Audit History**: Resubmission increments the attempt counter ($N \rightarrow N+1$) while immutably preserving the full historical audit trail across all iterations.
8. **Financial Isolation**: Creation and approval are strictly non-posting operations. Neither Accrued Payables nor General Ledgers are altered until final approval is achieved and an explicit disbursement action (`/pay`) is executed.
9. **Generic Reusability**: The core engine is decoupled from Party Payment via `WorkflowEntityType`, enabling rapid onboarding of Bill, Customer Payment, and Rake modules.

---

### 2. System Architecture & Design Patterns

The architecture cleanly segregates workflow definition from runtime execution:

```
[ Domain Modules ]                     [ Approval Engine ]
   Party Payment  ───(Entity Bridge)───►  ApprovalService
   Bill (Future)                           │
   Customer Pay (Future)                   ▼
                                       ApprovalInstanceEntity
                                       ├─ ApprovalInstanceLevelEntity (Snapshot)
                                       │   └─ ApprovalInstanceApproverEntity (Snapshot)
                                       └─ ApprovalActionEntity (Immutable Audit)
```

#### Core Components:
- **`ApprovalWorkflowEntity`**: Defines the workflow blueprint for a business entity (`PARTY_PAYMENT`).
- **`ApprovalWorkflowLevelEntity`**: Defines sequential tiers (`sequenceOrder`, `levelName`).
- **`ApprovalWorkflowApproverEntity`**: Associates users with workflow levels in configuration.
- **`ApprovalInstanceEntity`**: The runtime workflow execution container for a specific business document record (`entityType`, `entityId`).
- **`ApprovalInstanceLevelEntity`**: An immutable snapshot of the level structure frozen at the time of submission.
- **`ApprovalInstanceApproverEntity`**: An immutable snapshot of authorized users per level frozen at submission.
- **`ApprovalActionEntity`**: Append-only audit record detailing every user action (SUBMIT, APPROVE, REJECT, RESUBMIT, CANCEL).
- **`ApprovalService`**: Generic engine managing state transitions, concurrency locking, and level resolution.
- **`CommissionPaymentService`**: Business module orchestrator managing validation, draft creation, workflow delegation, and financial posting.

---

### 3. Configuration vs. Runtime Instance Model

A vital architectural requirement is the strict separation between **Workflow Configuration** and **Runtime Instances**:

| Feature | Configuration (`app_approval_workflow*`) | Runtime Instance (`app_approval_instance*`) |
| :--- | :--- | :--- |
| **Purpose** | Defines how approvals *should* work | Governs a specific running document voucher |
| **Mutability** | Editable by System Administrators | Created upon document submission; approver list is frozen |
| **Isolation** | Global blueprint per `WorkflowEntityType` | Scoped to individual `(entityType, entityId)` |
| **Admin Changes** | Future documents adopt updated levels | In-flight vouchers remain unaffected by admin changes |

When a voucher is submitted:
1. The engine checks for an existing `ApprovalInstanceEntity` for `(PARTY_PAYMENT, paymentId)`.
2. If absent, it queries the active `ApprovalWorkflowEntity` configuration and copies all active levels and approvers into `ApprovalInstanceLevelEntity` and `ApprovalInstanceApproverEntity` runtime snapshots.
3. If an instance exists (resubmission after rejection), it reactivates Level 1, increments `submissionCount`, and resets runtime levels to `PENDING`/`ACTIVE`.

---

### 4. The "ANY ONE" Approver Rule

Under enterprise ERP practices, awaiting consensus from every department manager causes administrative paralysis. The system strictly enforces the **ANY ONE** rule:

```
LEVEL 1 ACTIVE: [ Manager A, Manager B, Manager C ]
                          │
         Manager B approves (POST .../approve)
                          │
                          ▼
LEVEL 1 COMPLETED (completedBy = Manager B, completedAt = NOW)
LEVEL 2 ACTIVATED
[ Manager A, Manager C prevented from further action on Level 1 ]
```

#### Engine Enforcement Mechanics:
- When an approver submits an action, the engine executes inside `@Transactional(isolation = Isolation.READ_COMMITTED)`.
- The instance record is locked using `@Version` optimistic locking.
- The engine checks:
  1. Is `approvalInstance.overallStatus == PENDING_APPROVAL`?
  2. Is the user assigned to `currentLevelNumber`?
  3. Has the active level already been completed (`activeLevel.status == COMPLETED`)?
- Upon approval:
  - `activeLevel.status` becomes `COMPLETED`.
  - `activeLevel.completedBy` is set to the actor.
  - The next level (`sequenceOrder + 1`) is fetched.
  - If a next level exists, its status becomes `ACTIVE`.
  - If no further levels exist, `approvalInstance.overallStatus` transitions to `APPROVED`, and the business document status transitions to `APPROVED`.
- If a second user at Level 1 subsequently submits an approval request, the engine rejects the call:
  `"Approval level 1 is already completed."`

---

### 5. Maker-Checker & Separation of Duties

To comply with SOX and statutory financial controls, a document's creator/submitter must never possess approval authority over their own submission.

#### Enforcement Rules:
1. **Identification**: At submission, `submittedBy` is captured from `SecurityContextHolder`.
2. **Verification**: During `ApprovalService.approve()` or `ApprovalService.reject()`:
   ```java
   if (instance.getSubmittedBy() != null && instance.getSubmittedBy().equals(currentUser.getId())) {
       throw new BusinessException("Maker-checker violation: Submitter cannot approve their own submission.");
   }
   ```
3. **Frontend Awareness**: The frontend displays an informational banner and disables action buttons if `currentUser.id == payment.submittedBy`.

---

### 6. Complete Approval Lifecycle & State Transitions

```
[ DRAFT ] ──(Submit)──► [ PENDING_APPROVAL ] ──(Reject)──► [ REJECTED ]
                               │                                │
                       (L1 -> L2 -> L3)                    (Resubmit)
                               │                                │
                               ▼                                ▼
                         [ APPROVED ]                [ PENDING_APPROVAL ]
                               │
                          (POST /pay)
                               │
                               ▼
                            [ PAID ]
```

#### Detailed State Transition Matrix:

| Initial State | Action Trigger | Actor | Pre-Conditions | Resulting State | Business Effect |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **None** | `POST .../commission-payments` | Operator | Valid party, positive amount, valid allocations | `DRAFT` | Allocations saved with `status = DRAFT`. Payables unchanged. |
| **`DRAFT`** | `POST .../{id}/submit` | Submitter | User has `canSubmit` permission | `PENDING_APPROVAL` | Snapshot created, Level 1 set to `ACTIVE`. Submitter recorded. |
| **`PENDING_APPROVAL`** | `POST .../{id}/approve` | Level 1 Approver | Member of L1; Actor $\ne$ Submitter | `PENDING_APPROVAL` (L2 Active) | L1 completed. L2 activated. Audit action logged. |
| **`PENDING_APPROVAL`** | `POST .../{id}/approve` | Level 2 Approver | Member of L2; L1 completed; Actor $\ne$ Submitter | `PENDING_APPROVAL` (L3 Active) | L2 completed. L3 activated. Audit action logged. |
| **`PENDING_APPROVAL`** | `POST .../{id}/approve` | Level 3 Approver | Member of L3; L2 completed; Final Level | `APPROVED` | All levels complete. Instance `overallStatus = APPROVED`. Voucher status = `APPROVED`. |
| **`PENDING_APPROVAL`** | `POST .../{id}/reject` | Active Level Approver | Member of active level; Actor $\ne$ Submitter; Reason provided | `REJECTED` | Workflow halted. Reason stored. Submitter notified. |
| **`REJECTED`** | `POST .../{id}/submit` | Submitter | User has `canSubmit` permission | `PENDING_APPROVAL` | `submissionCount` incremented. Level 1 reactivated. History preserved. |
| **`APPROVED`** | `POST .../{id}/pay` | Finance Disburser | User has `canApprove` permission; Voucher `APPROVED` | `PAID` | Financial posting: Accrued payables deducted; Ledger updated. |
| **`DRAFT` / `REJECTED`** | `POST .../{id}/cancel` | Submitter / Admin | Status is DRAFT or REJECTED | `CANCELLED` | Voucher deactivated. Allocations deactivated. |

---

### 7. Rejection Handling & Resubmission Flow

Rejection is a non-destructive corrective intervention:
1. **Mandatory Justification**: The endpoint requires a non-empty `reason` string (validated via `@NotBlank`).
2. **Immediate Halt**: The active level is marked `REJECTED`. The approval instance transitions to `REJECTED`. The party payment status transitions to `REJECTED`.
3. **Audit Trail Preservation**: An `ApprovalActionEntity` record of type `REJECT` is stored with the exact user id, name, timestamp, and comments.
4. **Correction Window**: The submitter views the rejection reason on the voucher details page, modifies payment parameters if needed, and clicks **Resubmit for Approval**.
5. **Resubmission Execution**:
   - `submissionCount` is incremented (e.g. Attempt #2).
   - A `RESUBMIT` audit record is appended to the timeline.
   - Level 1 is set to `ACTIVE`. Future levels are reset to `PENDING`.
   - The entire approval cycle begins afresh from Level 1.

---

### 8. Financial Isolation & Posting Mechanism

A critical flaw in standard systems is premature ledger posting. Incentive ERP enforces **strict financial isolation**:

#### Phase 1: Creation & Drafting (`POST .../commission-payments`)
- Payment record created with status `DRAFT`.
- Payment allocations (`tx_party_payment_allocation`) created with `status = 'DRAFT'`.
- **Zero modification** to `tx_party_payable`:
  - `paidAmount` remains untouched.
  - `outstandingAmount` remains untouched.
  - `paymentStatus` remains `PENDING` / `PARTIAL`.
- **Zero modification** to party ledger.

#### Phase 2: Workflow Progression (`submit` $\rightarrow$ `approve` $\rightarrow$ `reject`)
- Purely governance and audit state updates.
- Financial balances remain unmodified.

#### Phase 3: Financial Disbursement (`POST .../{id}/pay`)
- Executed only after the voucher achieves `APPROVED` status.
- Atomically runs within a single `@Transactional` boundary:
  1. Voucher status transitions from `APPROVED` to `PAID`.
  2. Each allocation row status transitions from `DRAFT` to `PAID`.
  3. `tx_party_payable` records are updated:
     $$\text{paidAmount} \leftarrow \text{paidAmount} + \text{allocatedAmount}$$
     $$\text{outstandingAmount} \leftarrow \text{originalAmount} - \text{paidAmount}$$
     $$\text{paymentStatus} \leftarrow (\text{outstandingAmount} \le 0) \ ? \ \text{'PAID'} \ : \ \text{'PARTIAL'}$$
  4. Party Ledger service queries only `PAID` vouchers, reflecting the disbursement immediately in ledger statements.

---

### 9. Database Schema & Entity-Relationship Design

```
+-----------------------------------+        +----------------------------------------+
|    app_approval_workflow          |        |     app_approval_instance              |
+-----------------------------------+        +----------------------------------------+
| id               BIGINT (PK)      |        | id                  BIGINT (PK)        |
| workflow_name    VARCHAR(100)     |◄──┐    | workflow_id         BIGINT (FK)        |
| entity_type      VARCHAR(50)      |   │    | entity_type         VARCHAR(50)        |
| is_active        TINYINT(1)       |   │    | entity_id           BIGINT             |
| description      VARCHAR(255)     |   │    | overall_status      VARCHAR(30)        |
+-----------------+-----------------+   │    | current_level_number INT                |
                  │ 1                   │    | total_levels        INT                |
                  ▼ *                   │    | submitted_by        BIGINT             |
+-----------------+-----------------+   │    | submitted_at        DATETIME           |
|  app_approval_workflow_level      |   │    | completed_at        DATETIME           |
+-----------------------------------+   │    | submission_count    INT                |
| id               BIGINT (PK)      |   │    | version             BIGINT (Optimistic)|
| workflow_id      BIGINT (FK)──────┼───┘    +--------------------+-------------------+
| level_number     INT              |                             │ 1
| level_name       VARCHAR(100)     |                             ▼ *
| sequence_order   INT              |        +--------------------+-------------------+
| is_active        TINYINT(1)       |        |   app_approval_instance_level          |
+-----------------+-----------------+        +----------------------------------------+
                  │ 1                        | id                  BIGINT (PK)        |
                  ▼ *                        | instance_id         BIGINT (FK)        |
+-----------------+-----------------+        | level_number        INT                |
|  app_approval_workflow_approver   |        | level_name          VARCHAR(100)       |
+-----------------------------------+        | sequence_order      INT                |
| id               BIGINT (PK)      |        | status              VARCHAR(30)        |
| workflow_level_id BIGINT (FK)     |        | activated_at        DATETIME           |
| user_id          BIGINT (FK)      |        | completed_at        DATETIME           |
| is_active        TINYINT(1)       |        | completed_by        BIGINT (FK)        |
+-----------------------------------+        +--------------------+-------------------+
                                                                  │ 1
                                                                  ▼ *
                                             +--------------------+-------------------+
                                             |  app_approval_instance_approver        |
                                             +----------------------------------------+
                                             | id                  BIGINT (PK)        |
                                             | instance_level_id   BIGINT (FK)        |
                                             | user_id             BIGINT (FK)        |
                                             +----------------------------------------+
                                                                  │
                                                                  ▼ *
                                             +--------------------+-------------------+
                                             |     app_approval_action                |
                                             +----------------------------------------+
                                             | id                  BIGINT (PK)        |
                                             | instance_id         BIGINT (FK)        |
                                             | instance_level_id   BIGINT (FK)        |
                                             | action_type         VARCHAR(30)        |
                                             | action_by           BIGINT (FK)        |
                                             | action_at           DATETIME           |
                                             | remarks             TEXT               |
                                             | attempt_number      INT                |
                                             +----------------------------------------+
```

---

### 10. Audit Logging & Concurrency Control

#### Optimistic Locking
The `ApprovalInstanceEntity` contains a `@Version` field (`version`). Concurrent approval submissions at the same level will cause one transaction to succeed and subsequent concurrent transactions to encounter an `OptimisticLockingFailureException`. The second caller receives a clear business error indicating that the level has already transitioned.

#### Audit Trail Guarantee
Every lifecycle event writes an immutable row into `app_approval_action`:
- `SUBMIT`: Initial voucher routing.
- `APPROVE`: Level progression.
- `REJECT`: Level and workflow halt with mandatory remarks.
- `RESUBMIT`: Workflow reactivation after correction.
- `PAY`: Financial disbursement.
- `CANCEL`: Document voiding.

---

### 11. Role-Based Access Control (RBAC) & Designation Permissions

The existing designation permission matrix was upgraded with three dedicated approval privileges:
- `can_submit`: Authorization to submit documents into the approval pipeline.
- `can_approve`: Authorization to execute level approvals and payments.
- `can_reject`: Authorization to execute rejections.

#### Database Column Expansion:
Added to `mst_designation_page_permission`:
```sql
ALTER TABLE mst_designation_page_permission
  ADD COLUMN can_submit TINYINT(1) DEFAULT 0,
  ADD COLUMN can_approve TINYINT(1) DEFAULT 0,
  ADD COLUMN can_reject TINYINT(1) DEFAULT 0;
```

---

### 12. Seed Data & Test Personas

The system includes automatic database seeding in `ApprovalWorkflowDataInitializer.java`:

#### Seeded Users & Personas:

| Username | Password | Designation | Assigned Level | System Role / Persona |
| :--- | :--- | :--- | :--- | :--- |
| `rahul_exec` | `password123` | Operations Executive | Submitter | Maker (Creator & Submitter) |
| `manager_a` | `password123` | Site Manager | Level 1 | Level 1 Approver (Site Management) |
| `manager_b` | `password123` | Assistant Manager | Level 1 | Level 1 Approver (Site Management) |
| `finance_head`| `password123`| Finance Head | Level 2 | Level 2 Approver (Finance & Accounts) |
| `accounts_head`| `password123`| Accounts Head | Level 2 | Level 2 Approver (Finance & Accounts) |
| `biz_head` | `password123` | Business Head | Level 3 | Level 3 Approver (Executive Directorate) |
| `director_priya`| `password123`| Director | Level 3 | Level 3 Approver (Executive Directorate) |

#### Default Workflow: `PARTY_PAYMENT_APPROVAL`
- **Level 1 (Site Management)**: Approvers: `manager_a`, `manager_b`
- **Level 2 (Finance & Accounts)**: Approvers: `finance_head`, `accounts_head`
- **Level 3 (Executive Directorate)**: Approvers: `biz_head`, `director_priya`

---

### 13. Step-by-Step Testing & Verification Guide

#### Test Scenario A: Happy Path Sequential Approval (L1 $\rightarrow$ L2 $\rightarrow$ L3 $\rightarrow$ Pay)
1. **Login as Submitter (`rahul_exec`)**:
   - `POST /api/auth/login` with `{"username": "rahul_exec", "password": "password123"}`.
   - Note the JWT bearer token.
2. **Create Draft Payment**:
   - `POST /api/transaction/commission-payments`
   - Response: `status = "DRAFT"`. Verify payables table is NOT modified.
3. **Submit for Approval**:
   - `POST /api/transaction/commission-payments/{id}/submit` with `{"remarks": "Submitted for approval"}`.
   - Response: `status = "PENDING_APPROVAL"`. Workflow Level 1 is now `ACTIVE`.
4. **Attempt Unauthorized Early Approval (Level 2 as `finance_head`)**:
   - Login as `finance_head`.
   - `POST /api/transaction/commission-payments/{id}/approve`.
   - Expect HTTP 400/403: `"User is not an authorized approver for the active level."`
5. **Approve Level 1 (`manager_a`)**:
   - Login as `manager_a`.
   - `POST /api/transaction/commission-payments/{id}/approve` with `{"remarks": "Site operations verified."}`.
   - Level 1 status becomes `COMPLETED`. Level 2 status becomes `ACTIVE`.
6. **Verify "Any One" Rule (Duplicate Approval by `manager_b`)**:
   - Login as `manager_b`.
   - `POST /api/transaction/commission-payments/{id}/approve`.
   - Expect HTTP 400: `"Approval level 1 is already completed."` (or Level 1 is no longer active).
7. **Approve Level 2 (`finance_head`)**:
   - Login as `finance_head`.
   - `POST /api/transaction/commission-payments/{id}/approve` with `{"remarks": "Financials verified."}`.
   - Level 2 status becomes `COMPLETED`. Level 3 status becomes `ACTIVE`.
8. **Approve Level 3 (`director_priya`)**:
   - Login as `director_priya`.
   - `POST /api/transaction/commission-payments/{id}/approve` with `{"remarks": "Final executive sign-off."}`.
   - All levels complete. Voucher status becomes `APPROVED`.
9. **Disburse & Post Payment**:
   - Login as `finance_head`.
   - `POST /api/transaction/commission-payments/{id}/pay`.
   - Voucher status becomes `PAID`.
   - Verify `tx_party_payable` records have their `paidAmount` incremented and `paymentStatus` updated.
   - Verify party ledger statement reflects the payment.

---

### 14. Postman Workflow Scenarios

A complete Postman collection is exported at `document/party-payment-approval.postman_collection.json` containing:
1. `Auth - Login as Submitter (rahul_exec)`
2. `Auth - Login as Level 1 Approver (manager_a)`
3. `Auth - Login as Level 1 Alternate Approver (manager_b)`
4. `Auth - Login as Level 2 Approver (finance_head)`
5. `Auth - Login as Level 3 Approver (director_priya)`
6. `Payment - 01. Create Draft Voucher`
7. `Payment - 02. Submit Voucher (rahul_exec)`
8. `Payment - 03. Negative - Maker-Checker Self Approval (rahul_exec)`
9. `Payment - 04. Negative - Early Level 2 Approval (finance_head)`
10. `Payment - 05. Approve Level 1 (manager_a)`
11. `Payment - 06. Negative - Duplicate Level 1 Approval (manager_b)`
12. `Payment - 07. Approve Level 2 (finance_head)`
13. `Payment - 08. Approve Level 3 (director_priya)`
14. `Payment - 09. Disburse & Post Payment (finance_head)`
15. `Payment - 10. Rejection & Resubmission Flow (Alternative Run)`

---

### 15. Verification SQL Queries

Execute these queries in MySQL workbench or CLI to inspect state:

```sql
-- 1. Inspect Workflow Configuration
SELECT w.id, w.workflow_name, w.entity_type, l.level_number, l.level_name, u.username
FROM app_approval_workflow w
JOIN app_approval_workflow_level l ON l.workflow_id = w.id
JOIN app_approval_workflow_approver a ON a.workflow_level_id = l.id
JOIN mst_user u ON u.id = a.user_id
ORDER BY w.id, l.sequence_order, u.username;

-- 2. Inspect Running Approval Instances
SELECT id, entity_type, entity_id, overall_status, current_level_number, total_levels, submission_count, version
FROM app_approval_instance
ORDER BY id DESC;

-- 3. Inspect Runtime Level Statuses for a Payment Voucher
SELECT il.instance_id, il.level_number, il.level_name, il.status, il.activated_at, il.completed_at, u.username AS completed_by_user
FROM app_approval_instance_level il
LEFT JOIN mst_user u ON u.id = il.completed_by
WHERE il.instance_id = (SELECT id FROM app_approval_instance WHERE entity_type = 'PARTY_PAYMENT' AND entity_id = :paymentId)
ORDER BY il.sequence_order;

-- 4. Inspect Immutable Audit Action Log
SELECT a.id, a.action_type, u.username AS actor, a.action_at, a.remarks, a.attempt_number
FROM app_approval_action a
JOIN mst_user u ON u.id = a.action_by
WHERE a.instance_id = (SELECT id FROM app_approval_instance WHERE entity_type = 'PARTY_PAYMENT' AND entity_id = :paymentId)
ORDER BY a.action_at ASC;

-- 5. Inspect Party Payment Financial Integrity
SELECT p.id, p.payment_no, p.status, p.payment_amount, p.submitted_by, p.payment_type
FROM tx_commission_payment p
WHERE p.id = :paymentId;

-- 6. Verify Accrued Payables After Payment Disbursement
SELECT pa.accrued_payable_id, pa.allocated_amount, py.original_amount, py.paid_amount, py.outstanding_amount, py.payment_status
FROM tx_party_payment_allocation pa
JOIN tx_party_payable py ON py.id = pa.accrued_payable_id
WHERE pa.commission_payment_id = :paymentId;
```

---

### 16. REST API Reference

#### Voucher Lifecycle Endpoints:

| Method | Endpoint | Description | Request Body |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/transaction/commission-payments` | Creates a new payment voucher in `DRAFT` status | `CommissionPaymentRequest` |
| `POST` | `/api/transaction/commission-payments/{id}/submit` | Submits draft voucher into sequential approval pipeline | `{"remarks": "string"}` (optional) |
| `POST` | `/api/transaction/commission-payments/{id}/approve` | Approves active level for current user | `{"remarks": "string"}` (optional) |
| `POST` | `/api/transaction/commission-payments/{id}/reject` | Rejects active level; stops workflow | `{"reason": "string"}` (**mandatory**) |
| `POST` | `/api/transaction/commission-payments/{id}/pay` | Disburses and financially posts `APPROVED` voucher | None |
| `POST` | `/api/transaction/commission-payments/{id}/cancel` | Cancels draft or rejected voucher | `{"reason": "string"}` (optional) |
| `GET` | `/api/transaction/commission-payments/{id}` | Fetches voucher metadata and allocations | None |
| `GET` | `/api/transaction/commission-payments/{id}/approval` | Fetches runtime approval instance, levels, actions, and current user permissions | None |
| `GET` | `/api/transaction/commission-payments/vouchers` | Paginated listing with filtering by status and party | Query params (`status`, `partyId`, `page`, `size`) |

#### Admin Workflow Configuration Endpoints:

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/approval/workflows` | Lists all workflow configurations |
| `GET` | `/api/approval/workflows/{id}` | Fetches workflow details with levels and approvers |
| `POST` | `/api/approval/workflows` | Creates a new workflow blueprint |
| `PUT` | `/api/approval/workflows/{id}` | Updates existing workflow blueprint |

---

### 17. Frontend UI Components & User Experience

The React frontend has been augmented with full approval governance:
- **`ApprovalProgressStepper.tsx`**: Visual multi-node stepper displaying all sequential levels, active indicator, completed stamps with approver names, and pending indicators.
- **`ApprovalActionDialog.tsx`**: Dynamic modal dialog handling submission, approval verification, mandatory rejection justification, disbursement confirmation, and cancellation.
- **`ApprovalHistoryTimeline.tsx`**: Audit timeline showing chronological progression, badge colors, timestamps, usernames, designations, and rejection reasons.
- **`PartyPaymentDetailPage.tsx`**: Complete voucher management screen displaying voucher metadata, stepper, payment annexure table, action buttons with maker-checker disabled tooltips, and audit timeline.
- **`PartyPaymentPage.tsx`**: Two-tab interface separating aggregate Party Balances from individual Payment Vouchers & Approval Status.
