package org.example.incentivebackend.module.bulkimport.repository;

import org.example.incentivebackend.module.bulkimport.entity.ImportSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImportSessionRepository extends JpaRepository<ImportSessionEntity, Long> {
    Optional<ImportSessionEntity> findByImportId(String importId);
}
