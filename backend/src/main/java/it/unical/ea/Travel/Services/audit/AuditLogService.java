package it.unical.ea.Travel.Services.audit;

import it.unical.ea.Travel.Config.SecurityUtils;
import it.unical.ea.Travel.Entities.audit.AuditLog;
import it.unical.ea.Travel.Repositories.audit.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("security-audit");

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityName, String entityId, String details) {
        String actor = SecurityUtils.getCurrentUserEmail();
        
        logger.info("AUDIT_EVENT: actor={}, action={}, entityName={}, entityId={}, details={}", 
                actor, action, entityName, entityId, details);

        AuditLog auditLog = new AuditLog();
        auditLog.setActor(actor);
        auditLog.setAction(action);
        auditLog.setEntityName(entityName);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(details);
        auditLog.setTimestamp(LocalDateTime.now());
        
        try {
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            logger.error("Failed to save audit log to DB: {}", e.getMessage(), e);
        }
    }

    public Page<AuditLog> getAuditLogs(String actor, String action, String entityName, Pageable pageable) {
        Specification<AuditLog> spec = (root, query, cb) -> cb.conjunction();

        if (actor != null && !actor.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("actor")), "%" + actor.toLowerCase() + "%"));
        }
        if (action != null && !action.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("action"), action));
        }
        if (entityName != null && !entityName.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("entityName"), entityName));
        }

        return auditLogRepository.findAll(spec, pageable);
    }
}
