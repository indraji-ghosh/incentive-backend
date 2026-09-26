package org.example.incentivebackend.module.master.designation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.incentivebackend.common.entity.BaseEntity;

@Entity
@Table(name = "mm_designation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DesignationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "designation_seq")
    @SequenceGenerator(name = "designation_seq", sequenceName = "mm_designation_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "designation_code", nullable = false, unique = true, length = 50)
    private String designationCode;

    @Column(name = "designation_name", nullable = false, length = 100)
    private String designationName;

    @Column(name = "designation_level", length = 50)
    private String level;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
