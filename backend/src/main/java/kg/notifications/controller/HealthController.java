package kg.notifications.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kg.notifications.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Health", description = "API для проверки работоспособности")
public class HealthController {
    
    @GetMapping("/health")
    @Operation(
            summary = "Проверка работоспособности",
            description = "Возвращает статус работоспособности сервиса"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("service", "Notification Service");
        health.put("version", "1.0.0");
        health.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(ApiResponse.success(health));
    }
    
    @GetMapping("/info")
    @Operation(
            summary = "Информация о сервисе",
            description = "Возвращает информацию о сервисе"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> info() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", "Notification Service");
        info.put("version", "1.0.0");
        info.put("description", "Centralized Notification Service for multi-channel message delivery");
        info.put("channels", new String[]{"EMAIL", "TELEGRAM", "SMS", "WHATSAPP"});
        
        return ResponseEntity.ok(ApiResponse.success(info));
    }
}
