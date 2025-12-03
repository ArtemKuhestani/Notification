package kg.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiClientDto {
    
    private Integer clientId;
    private String clientName;
    private String clientDescription;
    private String apiKeyPrefix;
    private Boolean isActive;
    private Integer rateLimit;
    private String[] allowedChannels;
    private String callbackUrlDefault;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
    private Long notificationCount;
}
