package it.unical.ea.Travel.dtos.audit;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class AuditLogDto {
    private UUID id;
    private LocalDateTime timestamp;
    private String actor;
    private String action;
    private String entityName;
    private String entityId;
    private String details;
}
