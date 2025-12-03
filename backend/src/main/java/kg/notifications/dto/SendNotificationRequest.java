package kg.notifications.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kg.notifications.entity.ChannelType;
import kg.notifications.entity.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendNotificationRequest {
    
    @NotNull(message = "Канал доставки обязателен")
    private ChannelType channel;
    
    @NotBlank(message = "Получатель обязателен")
    private String recipient;
    
    private String subject;
    
    @NotBlank(message = "Текст сообщения обязателен")
    private String message;
    
    private Priority priority = Priority.NORMAL;
    
    private String idempotencyKey;
    
    private String callbackUrl;
    
    private Map<String, Object> metadata;
}
