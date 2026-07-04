package it.unical.ea.Travel.Mappers.audit;

import it.unical.ea.Travel.Entities.audit.AuditLog;
import it.unical.ea.Travel.dtos.audit.AuditLogDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {
    AuditLogDto toDto(AuditLog auditLog);
    List<AuditLogDto> toDtoList(List<AuditLog> auditLogs);
}
