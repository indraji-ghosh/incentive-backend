package org.example.incentivebackend.module.bulkimport.repository;

import org.example.incentivebackend.module.bulkimport.entity.ImportRowResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportRowResultRepository extends JpaRepository<ImportRowResultEntity, Long> {
    List<ImportRowResultEntity> findByImportSessionId(Long importSessionId);
    List<ImportRowResultEntity> findByImportSessionIdOrderByRowNumberAsc(Long importSessionId);
}
