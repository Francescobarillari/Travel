package it.unical.ea.Travel.Controllers.admin;

import it.unical.ea.Travel.Entities.audit.AuditLog;
import it.unical.ea.Travel.Mappers.audit.AuditLogMapper;
import it.unical.ea.Travel.Services.audit.AuditLogService;
import it.unical.ea.Travel.dtos.audit.AuditLogDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Admin Audit Logs", description = "Endpoints per la consultazione degli audit log (riservato agli amministratori)")
public class AdminAuditLogController {

    private final AuditLogService auditLogService;
    private final AuditLogMapper auditLogMapper;

    @Operation(summary = "Ottieni la lista degli audit log filtrata e paginata")
    @GetMapping
    public Page<AuditLogDto> getAuditLogs(
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp,desc") String[] sort) {

        Sort.Direction direction = Sort.Direction.DESC;
        String property = "timestamp";

        if (sort != null && sort.length > 0) {
            String[] parts = sort[0].split(",");
            property = parts[0];
            if (parts.length > 1 && "asc".equalsIgnoreCase(parts[1])) {
                direction = Sort.Direction.ASC;
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));
        Page<AuditLog> logsPage = auditLogService.getAuditLogs(actor, action, entityName, pageable);

        return logsPage.map(auditLogMapper::toDto);
    }
}
