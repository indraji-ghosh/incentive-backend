package org.example.incentivebackend.module.bulkimport.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.entity.BaseEntity;
import org.example.incentivebackend.module.bulkimport.enums.ImportSessionStatus;

@Entity
@Table(name = "cm_import_session")
@Getter
@Setter
public class ImportSessionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "import_session_seq")
    @SequenceGenerator(name = "import_session_seq", sequenceName = "cm_import_session_seq", allocationSize = 1)
    @Column(name = "import_session_id")
    private Long id;

    @Column(name = "import_id", unique = true, nullable = false, length = 50)
    private String importId; // e.g. IMP-2026-000001

    @Column(name = "module_name", nullable = false, length = 100)
    private String moduleName; // e.g. CLIENT, SITE

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ImportSessionStatus status;

    @Column(name = "total_rows")
    private Integer totalRows = 0;

    @Column(name = "valid_rows")
    private Integer validRows = 0;

    @Column(name = "invalid_rows")
    private Integer invalidRows = 0;

    @Column(name = "imported_rows")
    private Integer importedRows = 0;

    @Column(name = "failed_rows")
    private Integer failedRows = 0;
}
