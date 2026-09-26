package org.example.incentivebackend.module.master.designationpermission.repository;

import org.example.incentivebackend.module.master.designationpermission.entity.DesignationPagePermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignationPagePermissionRepository extends JpaRepository<DesignationPagePermissionEntity, Long> {

    List<DesignationPagePermissionEntity> findByDesignationId(Long designationId);

    Optional<DesignationPagePermissionEntity> findByDesignationIdAndPageId(Long designationId, Long pageId);

    @Query("SELECT p FROM DesignationPagePermissionEntity p JOIN FETCH p.page WHERE p.designation.id = :designationId")
    List<DesignationPagePermissionEntity> findByDesignationIdWithPage(@Param("designationId") Long designationId);
    
    @Query("SELECT p FROM DesignationPagePermissionEntity p WHERE p.designation.id = :designationId AND p.page.pageCode = :pageCode")
    Optional<DesignationPagePermissionEntity> findByDesignationIdAndPageCode(@Param("designationId") Long designationId, @Param("pageCode") String pageCode);
}
