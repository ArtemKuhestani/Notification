package kg.notifications.service;

import kg.notifications.dto.AuditLogDto;
import kg.notifications.entity.AuditLog;
import kg.notifications.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {
    
    private final AuditLogRepository auditLogRepository;
    
    public Page<AuditLogDto> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        ).map(this::toDto);
    }
    
    public Page<AuditLogDto> getAuditLogsByEntityType(String entityType, Pageable pageable) {
        return auditLogRepository.findByEntityType(entityType, pageable).map(this::toDto);
    }
    
    public Page<AuditLogDto> getAuditLogsByActionType(String actionType, Pageable pageable) {
        return auditLogRepository.findByActionType(actionType, pageable).map(this::toDto);
    }
    
    private AuditLogDto toDto(AuditLog log) {
        return AuditLogDto.builder()
                .logId(log.getLogId())
                .adminId(log.getAdmin() != null ? log.getAdmin().getAdminId() : null)
                .adminEmail(log.getAdmin() != null ? log.getAdmin().getEmail() : "system")
                .actionType(log.getActionType())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
