package org.example.incentivebackend.module.transaction.partyentry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.entity.BaseEntity;
import org.example.incentivebackend.module.master.businessHead.entity.BusinessHeadEntity;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.example.incentivebackend.module.master.paymenttype.entity.PaymentTypeEntity;
import org.example.incentivebackend.module.master.site.entity.SiteEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "txn_party_entry")
@Getter
@Setter
public class PartyEntryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "party_name", nullable = false, length = 150)
    private String partyName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_head_id", nullable = false)
    private BusinessHeadEntity businessHead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_type_id", nullable = false)
    private PaymentTypeEntity paymentType;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "remarks", length = 500)
    private String remarks;

    // Payment Information
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

    // Relationships
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "txn_party_entry_client",
            joinColumns = @JoinColumn(name = "party_entry_id"),
            inverseJoinColumns = @JoinColumn(name = "client_id")
    )
    private List<ClientEntity> clients = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "txn_party_entry_site",
            joinColumns = @JoinColumn(name = "party_entry_id"),
            inverseJoinColumns = @JoinColumn(name = "site_id")
    )
    private List<SiteEntity> sites = new ArrayList<>();

    @OneToMany(mappedBy = "partyEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartyUnitConfigurationEntity> unitConfigurations = new ArrayList<>();

    public void addUnitConfiguration(PartyUnitConfigurationEntity unitConfiguration) {
        unitConfigurations.add(unitConfiguration);
        unitConfiguration.setPartyEntry(this);
    }

    public void removeUnitConfiguration(PartyUnitConfigurationEntity unitConfiguration) {
        unitConfigurations.remove(unitConfiguration);
        unitConfiguration.setPartyEntry(null);
    }
}
