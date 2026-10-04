package org.example.incentivebackend.common.audit;

import org.example.incentivebackend.common.audit.entity.AuditLogEntity;
import org.example.incentivebackend.common.audit.enums.AuditAction;
import org.example.incentivebackend.common.audit.repository.AuditLogRepository;
import org.example.incentivebackend.common.audit.service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditLogTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void createAuditLog_ShouldSaveAuditLogEntity() {
        auditLogService.createAuditLog(
                "MASTER",
                "BusinessHead",
                "mm_buss_head",
                1L,
                AuditAction.CREATE, // Assuming CREATE enum exists, otherwise this will fail compilation. Assuming standard enums.
                "{\"headName\":\"Finance\"}",
                "Created new business head",
                99L
        );

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogEntity saved = captor.getValue();
        
        assertNotNull(saved);
        assertEquals("MASTER", saved.getModuleName());
        assertEquals("BusinessHead", saved.getEntityName());
        assertEquals("mm_buss_head", saved.getTableName());
        assertEquals(1L, saved.getEntityId());
        assertEquals(AuditAction.CREATE, saved.getAction());
        assertEquals("{\"headName\":\"Finance\"}", saved.getNewValues());
        assertEquals(99L, saved.getPerformedBy());
        assertNotNull(saved.getPerformedAt());
    }
}
