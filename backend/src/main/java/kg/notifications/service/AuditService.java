package kg.notifications.service;

import kg.notifications.entity.AuditLog;
import kg.notifications.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    
    @Async
    @Transactional
    public void logAction(Integer adminId, String actionType, String entityType, 
                         String entityId, Map<String, Object> oldValue, 
                         Map<String, Object> newValue, String ipAddress, String userAgent) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .actionType(actionType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .ipAddress(ipAddress != null ? ipAddress : "0.0.0.0")
                    .userAgent(userAgent)
                    .build();
            
            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} - {} - {}", actionType, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }
    
    @Async
    @Transactional
    public void logNotificationSend(String notificationId, String channel, String recipient, 
                                   String status, String ipAddress) {
        logAction(null, "SEND_NOTIFICATION", "NOTIFICATION", notificationId,
                null, Map.of(
                        "channel", channel,
                        "recipient", maskRecipient(recipient),
                        "status", status
                ), ipAddress, null);
    }
    
    @Async
    @Transactional
    public void logNotificationStatusChange(String notificationId, String oldStatus, 
                                           String newStatus, String errorMessage) {
        logAction(null, "STATUS_CHANGE", "NOTIFICATION", notificationId,
                Map.of("status", oldStatus),
                Map.of("status", newStatus, "error", errorMessage != null ? errorMessage : ""),
                "system", "NotificationService");
    }
    
    private String maskRecipient(String recipient) {
        if (recipient == null) return null;
        if (recipient.contains("@")) {
            // Mask email
            int atIndex = recipient.indexOf("@");
            if (atIndex > 2) {
                return recipient.substring(0, 2) + "***" + recipient.substring(atIndex);
            }
        }
        // Mask phone or other
        if (recipient.length() > 4) {
            return recipient.substring(0, 2) + "***" + recipient.substring(recipient.length() - 2);
        }
        return "***";
    }
}
