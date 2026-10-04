package org.example.incentivebackend.module.bulkimport.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.entity.BaseEntity;
import org.example.incentivebackend.module.bulkimport.enums.ImportRowStatus;

@Entity
@Table(name = "cm_import_row_result")
@Getter
@Setter
public class ImportRowResultEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "import_row_result_seq")
    @SequenceGenerator(name = "import_row_result_seq", sequenceName = "cm_import_row_result_seq", allocationSize = 1)
    @Column(name = "row_result_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_session_id", nullable = false)
    private ImportSessionEntity importSession;

    @Column(name = "row_number", nullable = false)
    private Integer rowNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ImportRowStatus status;

    @Column(name = "raw_data", columnDefinition = "CLOB")
    private String rawData; // JSON representation of the row data

    @Column(name = "errors", columnDefinition = "CLOB")
    private String errors; // JSON representation of errors/warnings
}
