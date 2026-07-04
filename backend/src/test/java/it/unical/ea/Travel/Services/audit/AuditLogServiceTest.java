package it.unical.ea.Travel.Services.audit;

import it.unical.ea.Travel.Entities.audit.AuditLog;
import it.unical.ea.Travel.Repositories.audit.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void shouldSaveAuditLog() {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("CREATE_ACTIVITY");
        auditLog.setEntityName("Activity");
        auditLog.setEntityId("123");
        auditLog.setDetails("Created test activity");

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);

        auditLogService.log("CREATE_ACTIVITY", "Activity", "123", "Created test activity");

        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFindAuditLogs() {
        AuditLog auditLog = new AuditLog();
        auditLog.setActor("SYSTEM");
        auditLog.setAction("CREATE_ACTIVITY");

        Page<AuditLog> page = new PageImpl<>(Collections.singletonList(auditLog));
        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<AuditLog> result = auditLogService.getAuditLogs("SYSTEM", "CREATE_ACTIVITY", "Activity", PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("SYSTEM", result.getContent().get(0).getActor());
        verify(auditLogRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }
}
