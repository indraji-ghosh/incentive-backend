package org.example.incentivebackend.module.master.sector.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.entity.BaseEntity;
import org.example.incentivebackend.common.enums.StatusEnum;

@Entity
@Table(
        name = "mm_sector",
        indexes = {
                @Index(
                        name = "IDX_MM_SECTOR_STATUS",
                        columnList = "sector_status"
                ),
                @Index(
                        name = "IDX_MM_SECTOR_NAME",
                        columnList = "sector_name"
                )
        }
)
@Getter
@Setter
public class SectorEntity extends BaseEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "sector_seq"
    )
    @SequenceGenerator(
            name = "sector_seq",
            sequenceName = "mm_sector_seq",
            allocationSize = 1
    )
    @Column(name = "sector_id")
    private Long sectorId;

    @Column(
            name = "sector_name",
            nullable = false,
            length = 150
    )
    private String sectorName;

    @Column(
            name = "sector_short_code",
            nullable = false,
            length = 20,
            unique = true
    )
    private String sectorShortCode;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "sector_status",
            nullable = false,
            length = 20
    )
    private StatusEnum sectorStatus;
}