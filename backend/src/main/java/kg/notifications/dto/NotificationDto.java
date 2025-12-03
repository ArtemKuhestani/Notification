package kg.notifications.dto;

import kg.notifications.entity.ChannelType;
import kg.notifications.entity.NotificationStatus;
import kg.notifications.entity.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    
    private UUID notificationId;
    private Integer clientId;
    private String clientName;
    private ChannelType channelType;
    private String recipient;
    private String subject;
    private String messageBody;
    private NotificationStatus status;
    private Priority priority;
    private Integer retryCount;
    private Integer maxRetries;
    private LocalDateTime nextRetryAt;
    private String errorMessage;
    private String errorCode;
    private String providerMessageId;
    private String idempotencyKey;
    private String callbackUrl;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime sentAt;
    private LocalDateTime expiresAt;
}
