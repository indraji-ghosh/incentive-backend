# Incentive Project Entities

This document lists all the entity classes present in the Incentive Backend project, along with their fields and relationships.

**Note on `BaseEntity`:** All entities in this project (except explicitly stated otherwise) inherit from a common `BaseEntity` which provides the following auditing fields:
- `Long` `createdBy`
- `Long` `updatedBy`
- `LocalDateTime` `createdAt`
- `LocalDateTime` `updatedAt`
- `Boolean` `isActive`
## Association

### `ClientSiteEntity`
- **Table Name:** `UK_MM_CLIENT_SITE`
- **Fields:**
  - `Long` `clientSiteId` (@Column, @Id)
  - `ClientEntity` `client`
  - `SiteEntity` `site`

### `PartyAssignmentEntity`
- **Table Name:** `UK_PARTY_CLIENT_SITE`
- **Fields:**
  - `Long` `id` (@Column, @Id)
  - `PartyEntity` `party` (@ManyToOne)
  - `ClientEntity` `client` (@ManyToOne)
  - `SiteEntity` `site` (@ManyToOne)

### `PartyServiceConfigurationEntity`
- **Table Name:** `IDX_PSC_ASSIGNMENT`
- **Fields:**
  - `Long` `id` (@Column, @Id)
  - `PartyAssignmentEntity` `partyAssignment` (@ManyToOne)
  - `ServiceTypeEntity` `service` (@ManyToOne)
  - `PaymentTypeEntity` `paymentType` (@ManyToOne)
  - `UnitEntity` `unit` (@ManyToOne)
  - `BigDecimal` `rate` (@Column)
  - `LocalDate` `effectiveFrom` (@Column)
  - `LocalDate` `effectiveTo` (@Column)
  - `String` `notes` (@Column)

## Common

### `AuditLogEntity`
- **Table Name:** `idx_audit_module`
- **Fields:**
  - `Long` `auditLogId` (@Column)
  - `String` `moduleName` (@Column)
  - `String` `entityName` (@Column)
  - `String` `tableName` (@Column)
  - `Long` `entityId` (@Column)
  - `String` `data` (@Column)
  - `AuditAction` `action` (@Column, @Enumerated)
  - `String` `remarks` (@Column)
  - `Long` `performedBy` (@Column)
  - `LocalDateTime` `performedAt` (@Column)

### `BaseEntity`
- **Table Name:** `Unknown`
- **Fields:**
  - `LocalDateTime` `createdAt` (@Column)
  - `Long` `createdBy` (@Column)
  - `Long` `modBy` (@Column)
  - `LocalDateTime` `modAt` (@Column)
  - `String` `appStatus` (@Column)
  - `Long` `appBy` (@Column)
  - `LocalDateTime` `appAt` (@Column)

## Master

### `BusinessHeadEntity`
- **Table Name:** `IDX_MM_BUSS_HEAD_STATUS`
- **Fields:**
  - `Long` `headId` (@Column, @Id)
  - `String` `headName` (@Column)
  - `String` `headShortCode` (@Column)
  - `StatusEnum` `headStatus` (@Column, @Enumerated)

### `ClientEntity`
- **Table Name:** `IDX_MM_CLIENT_STATUS`
- **Fields:**
  - `Long` `clientId` (@Column)
  - `String` `clientName`
  - `String` `clientShortCode`
  - `StatusEnum` `clientStatus`

### `DesignationEntity`
- **Table Name:** `mm_designation`
- **Fields:**
  - `Long` `id` (@Column, @Id)
  - `String` `designationCode` (@Column)
  - `String` `designationName` (@Column)
  - `String` `level` (@Column)
  - `String` `description` (@Column)

### `DesignationPagePermissionEntity`
- **Table Name:** `mm_designation_page_permission`
- **Fields:**
  - `Long` `id` (@Column, @Id)
  - `DesignationEntity` `designation` (@ManyToOne)
  - `PageEntity` `page` (@ManyToOne)

### `PageEntity`
- **Table Name:** `mm_page`
- **Fields:**
  - `Long` `id` (@Column, @Id)
  - `String` `pageCode` (@Column)
  - `String` `pageName` (@Column)
  - `String` `moduleName` (@Column)
  - `String` `route` (@Column)
  - `String` `icon` (@Column)
  - `Integer` `displayOrder` (@Column)

### `PartyEntity`
- **Table Name:** `IDX_MM_PARTY_STATUS`
- **Fields:**
  - `Long` `id` (@Column, @Id)
  - `String` `partyName` (@Column)
  - `BusinessHeadEntity` `businessHead` (@ManyToOne)
  - `String` `contactPerson` (@Column)
  - `String` `contactNumber` (@Column)
  - `String` `email` (@Column)
  - `String` `accountHolderName` (@Column)
  - `String` `accountNo` (@Column)
  - `String` `ifscCode` (@Column)
  - `String` `bankName` (@Column)
  - `String` `branchName` (@Column)
  - `String` `remarks` (@Column)

### `PaymentTypeEntity`
- **Table Name:** `payment_type`
- **Fields:**
  - `Long` `id` (@Column, @Id)
  - `String` `code` (@Column)
  - `String` `name` (@Column)
  - `String` `description` (@Column)

### `SectorEntity`
- **Table Name:** `IDX_MM_SECTOR_STATUS`
- **Fields:**
  - `Long` `sectorId` (@Column)
  - `String` `sectorName`
  - `String` `sectorShortCode`
  - `StatusEnum` `sectorStatus`

### `ServiceTypeEntity`
- **Table Name:** `mst_service_type`
- **Fields:**
  - `Long` `id` (@Id)
  - `String` `name` (@Column)
  - `String` `description` (@Column)

### `SiteEntity`
- **Table Name:** `IDX_MM_SITE_SITE_STATUS`
- **Fields:**
  - `Long` `siteId` (@Column)
  - `String` `siteName`
  - `String` `siteShortCode`
  - `String` `state`
  - `StatusEnum` `siteStatus`

### `UnitEntity`
- **Table Name:** `unit_master`
- **Fields:**
  - `Long` `id` (@Column, @Id)
  - `String` `code` (@Column)
  - `String` `name` (@Column)
  - `String` `description` (@Column)

### `UserEntity`
- **Table Name:** `mm_user`
- **Fields:**
  - `Long` `userId` (@Column)
  - `String` `username` (@Column)
  - `String` `password` (@Column)
  - `String` `fullName` (@Column)
  - `String` `email` (@Column)
  - `String` `mobileNumber` (@Column)
  - `String` `role` (@Column)
  - `UserStatus` `status` (@Column, @Enumerated)
  - `DesignationEntity` `designation` (@ManyToOne)

## Transaction

### `BillAnnexureEntity`
- **Table Name:** `IDX_TX_BILL_ANNEXURE_BILL`
- **Fields:**
  - `Long` `billAnnexureId` (@Column, @Id)
  - `BillEntity` `bill`
  - `String` `rrNo`
  - `LocalDate` `rrDate` (@Column)
  - `String` `challan`
  - `LocalDate` `loadDate` (@Column)
  - `String` `siding`
  - `String` `destination`
  - `Integer` `wagons`

### `BillEntity`
- **Table Name:** `IDX_TX_BILL_CLIENT`
- **Fields:**
  - `Long` `billId` (@Column, @Id)
  - `String` `billNumber`
  - `LocalDate` `workingMonth`
  - `ClientEntity` `client`
  - `PartyEntryEntity` `party`
  - `SiteEntity` `site`
  - `BigDecimal` `billAmount`
  - `BigDecimal` `outstandingAmount`
  - `String` `remarks`

### `ClientPaymentEntity`
- **Table Name:** `IDX_TXN_CLIENT_PAYMENT_STATUS`
- **Fields:**
  - `Long` `clientPaymentId` (@Column)
  - `String` `paymentNo` (@Column)
  - `BillEntity` `bill` (@ManyToOne)
  - `ClientEntity` `client` (@ManyToOne)
  - `BigDecimal` `paymentAmount` (@Column)
  - `LocalDate` `paymentDate` (@Column)
  - `String` `remarks` (@Column)
  - `StatusEnum` `paymentStatus` (@Column, @Enumerated)

### `CommissionPaymentEntity`
- **Table Name:** `tr_commission_payment`
- **Fields:**
  - `Long` `commissionPaymentId` (@Column, @Id)
  - `String` `paymentNo` (@Column)
  - `PartyEntity` `party` (@ManyToOne)
  - `LocalDate` `paymentDate` (@Column)
  - `BigDecimal` `paymentAmount` (@Column)
  - `String` `remarks` (@Column)
  - `String` `status` (@Column)

### `PartyEntryEntity`
- **Table Name:** `txn_party_entry`
- **Fields:**
  - `Long` `id` (@Column, @Id)
  - `String` `partyName` (@Column)
  - `BusinessHeadEntity` `businessHead` (@ManyToOne)
  - `PaymentTypeEntity` `paymentType` (@ManyToOne)
  - `LocalDate` `effectiveFrom` (@Column)
  - `LocalDate` `effectiveTo` (@Column)
  - `String` `remarks` (@Column)
  - `String` `accountHolderName` (@Column)
  - `String` `accountNo` (@Column)
  - `String` `ifscCode` (@Column)
  - `String` `bankName` (@Column)
  - `String` `branchName` (@Column)

### `PartyPayableEntity`
- **Table Name:** `UK_PAYABLE_SOURCE_ASSIGN_SERVICE`
- **Fields:**
  - `Long` `id` (@Column, @Id)
  - `PartyEntity` `party` (@ManyToOne)
  - `PartyAssignmentEntity` `partyAssignment` (@ManyToOne)
  - `ServiceTypeEntity` `service` (@ManyToOne)
  - `PartyServiceConfigurationEntity` `partyServiceConfiguration` (@ManyToOne)
  - `String` `sourceType` (@Column)
  - `String` `sourceId` (@Column)
  - `String` `sourceReference` (@Column)
  - `String` `calculationBasis` (@Column)
  - `BigDecimal` `quantity` (@Column)
  - `BigDecimal` `rate` (@Column)
  - `BigDecimal` `payableAmount` (@Column)
  - `LocalDate` `transactionDate` (@Column)
  - `String` `remarks` (@Column)
  - `BigDecimal` `outstandingAmount` (@Column)

### `PartyPaymentAdjustmentEntity`
- **Table Name:** `tr_party_payment_adjustment`
- **Fields:**
  - `Long` `adjustmentId` (@Column, @Id)
  - `CommissionPaymentEntity` `commissionPayment` (@ManyToOne)
  - `PartyPayableEntity` `payable` (@ManyToOne)
  - `BigDecimal` `adjustedAmount` (@Column)
  - `BigDecimal` `allocatedAmount` (@Column)

### `PartyUnitConfigurationEntity`
- **Table Name:** `txn_party_unit_config`
- **Fields:**
  - `Long` `id` (@Column, @Id)
  - `PartyEntryEntity` `partyEntry` (@ManyToOne)
  - `UnitEntity` `unit` (@ManyToOne)
  - `org.example.incentivebackend.module.master.servicetype.entity.ServiceTypeEntity` `serviceType` (@ManyToOne)
  - `BigDecimal` `rate` (@Column)
  - `LocalDate` `effectiveFrom` (@Column)
  - `LocalDate` `effectiveTo` (@Column)
  - `String` `notes` (@Column)

### `RakeAnnexureEntity`
- **Table Name:** `IDX_TX_RAKE_ANNEXURE_RAKE`
- **Fields:**
  - `Long` `rakeAnnexureId` (@Column, @Id)
  - `RakeEntryEntity` `rakeEntry`
  - `String` `rrNo`
  - `LocalDate` `rrDate` (@Column)
  - `String` `challan`
  - `LocalDate` `loadDate` (@Column)
  - `String` `siding`
  - `String` `destination`
  - `Integer` `wagons`
  - `BigDecimal` `weight`
  - `BigDecimal` `quantity`
  - `BigDecimal` `annexureAmount`
  - `String` `notes`
  - `StatusEnum` `status` (@Column, @Enumerated)

### `RakeEntryEntity`
- **Table Name:** `IDX_TX_RAKE_ENTRY_CLIENT`
- **Fields:**
  - `Long` `rakeEntryId` (@Column, @Id)
  - `String` `rakeNumber`
  - `LocalDate` `workingMonth`
  - `ClientEntity` `client`
  - `PartyEntryEntity` `party`
  - `SiteEntity` `site`
  - `String` `remarks`
  - `StatusEnum` `rakeStatus` (@Column, @Enumerated)

