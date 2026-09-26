package org.example.incentivebackend.module.master.page.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.incentivebackend.common.entity.BaseEntity;

@Entity
@Table(name = "mm_page")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "page_seq")
    @SequenceGenerator(name = "page_seq", sequenceName = "mm_page_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "page_code", nullable = false, unique = true, length = 100)
    private String pageCode;

    @Column(name = "page_name", nullable = false, length = 150)
    private String pageName;

    @Column(name = "module_name", length = 100)
    private String moduleName;

    @Column(name = "route", length = 255)
    private String route;

    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
