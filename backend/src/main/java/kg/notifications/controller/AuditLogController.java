package kg.notifications.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kg.notifications.dto.ApiResponse;
import kg.notifications.dto.AuditLogDto;
import kg.notifications.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/audit")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit Logs", description = "API для просмотра журнала аудита")
public class AuditLogController {
    
    private final AuditLogService auditLogService;
    
    @GetMapping
    @Operation(
            summary = "Получить журнал аудита",
            description = "Возвращает список записей аудита с пагинацией"
    )
    public ResponseEntity<ApiResponse<Page<AuditLogDto>>> getAuditLogs(
            @PageableDefault(size = 50) Pageable pageable,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String actionType) {
        
        Page<AuditLogDto> logs;
        
        if (entityType != null) {
            logs = auditLogService.getAuditLogsByEntityType(entityType, pageable);
        } else if (actionType != null) {
            logs = auditLogService.getAuditLogsByActionType(actionType, pageable);
        } else {
            logs = auditLogService.getAuditLogs(pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
