package org.example.incentivebackend.module.master.businessHead.repository;

import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.master.businessHead.entity.BusinessHeadEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BusinessHeadRepositoryTest {

    @Autowired
    private BusinessHeadRepository repository;

    @Test
    void save_ShouldPersistBusinessHead() {
        BusinessHeadEntity entity = new BusinessHeadEntity();
        entity.setHeadName("Marketing");
        entity.setHeadShortCode("MKT");
        entity.setHeadStatus(StatusEnum.A);

        BusinessHeadEntity saved = repository.save(entity);

        assertNotNull(saved.getHeadId());
        assertEquals("Marketing", saved.getHeadName());
        assertEquals("MKT", saved.getHeadShortCode());
        assertEquals(StatusEnum.A, saved.getHeadStatus());
        assertNotNull(saved.getCreatedAt(), "Auditing should set createdAt");
    }

    @Test
    void findById_ShouldReturnEntity_WhenExists() {
        BusinessHeadEntity entity = new BusinessHeadEntity();
        entity.setHeadName("Sales");
        entity.setHeadShortCode("SLS");
        entity.setHeadStatus(StatusEnum.A);
        BusinessHeadEntity saved = repository.save(entity);

        Optional<BusinessHeadEntity> found = repository.findById(saved.getHeadId());

        assertTrue(found.isPresent());
        assertEquals(saved.getHeadId(), found.get().getHeadId());
    }

    @Test
    void existsByHeadShortCode_ShouldReturnTrue_WhenExists() {
        BusinessHeadEntity entity = new BusinessHeadEntity();
        entity.setHeadName("Engineering");
        entity.setHeadShortCode("ENG");
        entity.setHeadStatus(StatusEnum.A);
        repository.save(entity);

        boolean exists = repository.existsByHeadShortCode("ENG");
        assertTrue(exists);
    }

    @Test
    void existsByHeadShortCode_ShouldReturnFalse_WhenNotExists() {
        boolean exists = repository.existsByHeadShortCode("MISSING");
        assertFalse(exists);
    }

    @Test
    void findByHeadShortCode_ShouldReturnEntity_WhenExists() {
        BusinessHeadEntity entity = new BusinessHeadEntity();
        entity.setHeadName("Human Resources");
        entity.setHeadShortCode("HR");
        entity.setHeadStatus(StatusEnum.A);
        repository.save(entity);

        Optional<BusinessHeadEntity> found = repository.findByHeadShortCode("HR");

        assertTrue(found.isPresent());
        assertEquals("Human Resources", found.get().getHeadName());
    }

    //@Test
    void save_ShouldThrowException_WhenDuplicateShortCode() {
        BusinessHeadEntity entity1 = new BusinessHeadEntity();
        entity1.setHeadName("IT Support");
        entity1.setHeadShortCode("IT");
        entity1.setHeadStatus(StatusEnum.A);
        repository.saveAndFlush(entity1);

        BusinessHeadEntity entity2 = new BusinessHeadEntity();
        entity2.setHeadName("IT Infrastructure");
        entity2.setHeadShortCode("IT"); // Duplicate
        entity2.setHeadStatus(StatusEnum.A);

        assertThrows(DataIntegrityViolationException.class, () -> {
            repository.saveAndFlush(entity2);
        });
    }
}
