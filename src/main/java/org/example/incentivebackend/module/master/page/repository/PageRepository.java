package org.example.incentivebackend.module.master.page.repository;

import org.example.incentivebackend.module.master.page.entity.PageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Long> {
    Optional<PageEntity> findByPageCode(String pageCode);
    List<PageEntity> findAllByOrderByDisplayOrderAsc();
}
