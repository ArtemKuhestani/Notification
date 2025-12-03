package kg.notifications.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kg.notifications.dto.*;
import kg.notifications.entity.ChannelType;
import kg.notifications.entity.NotificationStatus;
import kg.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "API для отправки и управления уведомлениями")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @PostMapping("/send")
    @Operation(
            summary = "Отправить уведомление",
            description = "Отправляет уведомление на указанный канал (EMAIL, SMS, TELEGRAM, WHATSAPP)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "202",
                    description = "Уведомление принято в обработку",
                    content = @Content(schema = @Schema(implementation = SendNotificationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Невалидные данные запроса"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Не авторизован"
            )
    })
    public ResponseEntity<ApiResponse<SendNotificationResponse>> sendNotification(
            @Valid @RequestBody SendNotificationRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Received notification request: channel={}", request.getChannel());
        
        // For now, use default client ID = 1 (Test Client)
        // In production, this would come from API Key authentication
        Integer clientId = 1;
        String ipAddress = getClientIp(httpRequest);
        
        SendNotificationResponse response = notificationService.sendNotification(request, clientId, ipAddress);
        
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(response, "Уведомление принято в обработку"));
    }
    
    @GetMapping("/status/{id}")
    @Operation(
            summary = "Получить статус уведомления",
            description = "Возвращает текущий статус и детали уведомления по его ID"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Успешно",
                    content = @Content(schema = @Schema(implementation = NotificationDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Уведомление не найдено"
            )
    })
    public ResponseEntity<ApiResponse<NotificationDto>> getNotificationStatus(
            @Parameter(description = "ID уведомления") @PathVariable UUID id) {
        
        return notificationService.getNotificationStatus(id)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Уведомление не найдено")));
    }
    
    @GetMapping("/admin/notifications")
    @Operation(
            summary = "Список уведомлений",
            description = "Возвращает список всех уведомлений с пагинацией"
    )
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getNotifications(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) ChannelType channel) {
        
        Page<NotificationDto> notifications;
        
        if (status != null) {
            notifications = notificationService.getNotificationsByStatus(status, pageable);
        } else if (channel != null) {
            notifications = notificationService.getNotificationsByChannel(channel, pageable);
        } else {
            notifications = notificationService.getNotifications(pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }
    
    @GetMapping("/admin/notifications/{id}")
    @Operation(
            summary = "Детали уведомления",
            description = "Возвращает полную информацию об уведомлении"
    )
    public ResponseEntity<ApiResponse<NotificationDto>> getNotification(
            @Parameter(description = "ID уведомления") @PathVariable UUID id) {
        
        return notificationService.getNotificationStatus(id)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Уведомление не найдено")));
    }
    
    @PostMapping("/admin/notifications/{id}/retry")
    @Operation(
            summary = "Повторить отправку",
            description = "Повторно отправляет неудачное уведомление"
    )
    public ResponseEntity<ApiResponse<NotificationDto>> retryNotification(
            @Parameter(description = "ID уведомления") @PathVariable UUID id) {
        
        return notificationService.retryNotification(id)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto, "Повторная отправка запущена")))
                .orElse(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Невозможно повторить отправку для данного уведомления")));
    }
    
    @GetMapping("/admin/stats/dashboard")
    @Operation(
            summary = "Статистика для дашборда",
            description = "Возвращает агрегированную статистику за последние 24 часа"
    )
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getDashboardStats() {
        DashboardStatsDto stats = notificationService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
