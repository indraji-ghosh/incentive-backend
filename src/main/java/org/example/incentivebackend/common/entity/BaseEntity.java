package org.example.incentivebackend.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, nullable = true)
    private Long createdBy;

    @Column(name = "mod_no")
    private Integer modNo = 0;

    @LastModifiedBy
    @Column(name = "mod_by")
    private Long modBy;

    @LastModifiedDate
    @Column(name = "mod_at")
    private LocalDateTime modAt;

    @Column(name = "app_status")
    private String appStatus;

    @Column(name = "app_by")
    private Long appBy;

    @Column(name = "app_at")
    private LocalDateTime appAt;
}
