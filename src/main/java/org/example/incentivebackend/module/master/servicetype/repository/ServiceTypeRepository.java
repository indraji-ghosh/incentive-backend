package org.example.incentivebackend.module.master.servicetype.repository;

import org.example.incentivebackend.module.master.servicetype.entity.ServiceTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceTypeEntity, Long> {
}
