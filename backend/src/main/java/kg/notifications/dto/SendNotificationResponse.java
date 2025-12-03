package kg.notifications.dto;

import kg.notifications.entity.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendNotificationResponse {
    
    private UUID notificationId;
    private NotificationStatus status;
    private LocalDateTime createdAt;
    private String message;
}
