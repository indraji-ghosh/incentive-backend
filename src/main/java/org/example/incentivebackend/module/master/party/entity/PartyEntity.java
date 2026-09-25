package org.example.incentivebackend.module.master.party.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.audit.anotation.Auditable;
import org.example.incentivebackend.common.audit.enums.AuditModule;
import org.example.incentivebackend.common.entity.BaseEntity;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.master.businessHead.entity.BusinessHeadEntity;

@Entity
@Table(
        name = "mm_party",
        indexes = {
                @Index(name = "IDX_MM_PARTY_STATUS", columnList = "party_status"),
                @Index(name = "IDX_MM_PARTY_NAME", columnList = "party_name")
        }
)
@Auditable(
        module = AuditModule.MASTER,
        entity = "Party",
        table = "mm_party"
)
@Getter
@Setter
public class PartyEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "party_name", nullable = false, length = 150)
    private String partyName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_head_id")
    private BusinessHeadEntity businessHead;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @Column(name = "email", length = 100)
    private String email;

    // Payment & Bank Information
    @Column(name = "account_holder_name", length = 150)
    private String accountHolderName;

    @Column(name = "account_no", length = 50)
    private String accountNo;

    @Column(name = "ifsc_code", length = 20)
    private String ifscCode;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "branch_name", length = 100)
    private String branchName;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "party_status", nullable = false, length = 20)
    private StatusEnum partyStatus = StatusEnum.A;
}
