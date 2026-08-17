package org.example.incentivebackend.module.master.client.entity;


import jakarta.persistence.*;
        import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.audit.anotation.Auditable;
import org.example.incentivebackend.common.audit.enums.AuditModule;
import org.example.incentivebackend.common.entity.BaseEntity;
import org.example.incentivebackend.common.enums.StatusEnum;

@Auditable(
        module = AuditModule.MASTER,
        entity = "Client",
        table = "mm_client"
)
@Entity
@Table(
        name = "mm_client",
        indexes = {
                @Index(
                        name = "IDX_MM_CLIENT_STATUS",
                        columnList = "client_status"
                ),
                @Index(
                        name = "IDX_MM_CLIENT_SHORT_CODE",
                        columnList = "client_short_code"
                )
        }
)
@Getter
@Setter
public class ClientEntity extends BaseEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "client_seq"
    )
    @SequenceGenerator(
            name = "client_seq",
            sequenceName = "mm_client_seq",
            allocationSize = 1
    )
    @Column(name = "client_id")
    private Long clientId;

    @Column(
            name = "client_name",
            nullable = false,
            length = 150
    )
    private String clientName;

    @Column(
            name = "client_short_code",
            nullable = false,
            unique = true,
            length = 50
    )
    private String clientShortCode;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "client_status",
            nullable = false,
            length = 20
    )
    private StatusEnum clientStatus;
}