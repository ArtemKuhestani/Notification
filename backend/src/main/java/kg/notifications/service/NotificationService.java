package kg.notifications.service;

import kg.notifications.dto.*;
import kg.notifications.entity.*;
import kg.notifications.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final ApiClientRepository apiClientRepository;
    private final EmailService emailService;
    private final AuditService auditService;
    
    @Transactional
    public SendNotificationResponse sendNotification(SendNotificationRequest request, Integer clientId, String ipAddress) {
        log.info("Processing notification request: channel={}, recipient={}", 
                request.getChannel(), maskRecipient(request.getRecipient()));
        
        // Check for idempotency
        if (request.getIdempotencyKey() != null) {
            Optional<Notification> existing = notificationRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                log.info("Duplicate request detected with idempotency key: {}", request.getIdempotencyKey());
                Notification n = existing.get();
                return SendNotificationResponse.builder()
                        .notificationId(n.getNotificationId())
                        .status(n.getStatus())
                        .createdAt(n.getCreatedAt())
                        .message("Duplicate request - returning existing notification")
                        .build();
            }
        }
        
        // Get API client
        ApiClient client = apiClientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid client ID"));
        
        // Update last used
        client.setLastUsedAt(LocalDateTime.now());
        apiClientRepository.save(client);
        
        // Create notification
        Notification notification = Notification.builder()
                .client(client)
                .channelType(request.getChannel())
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .messageBody(request.getMessage())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.NORMAL)
                .status(NotificationStatus.PENDING)
                .retryCount(0)
                .maxRetries(5)
                .idempotencyKey(request.getIdempotencyKey())
                .callbackUrl(request.getCallbackUrl())
                .metadata(request.getMetadata())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        
        notification = notificationRepository.save(notification);
        log.info("Notification created: {}", notification.getNotificationId());
        
        // Log to audit
        auditService.logNotificationSend(
                notification.getNotificationId().toString(),
                request.getChannel().name(),
                request.getRecipient(),
                "PENDING",
                ipAddress);
        
        // Send asynchronously based on channel
        final Notification savedNotification = notification;
        if (request.getChannel() == ChannelType.EMAIL) {
            emailService.sendEmail(savedNotification);
        } else {
            // For other channels, just mark as pending for now
            log.info("Channel {} not yet implemented, notification {} waiting for processing", 
                    request.getChannel(), notification.getNotificationId());
        }
        
        return SendNotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .message("Notification accepted for processing")
                .build();
    }
    
    public Optional<NotificationDto> getNotificationStatus(UUID notificationId) {
        return notificationRepository.findById(notificationId)
                .map(this::toDto);
    }
    
    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotifications(Pageable pageable) {
        return notificationRepository.findAllWithClient(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize()
                )
        ).map(this::toDto);
    }
    
    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotificationsByStatus(NotificationStatus status, Pageable pageable) {
        return notificationRepository.findByStatusWithClient(status, pageable).map(this::toDto);
    }
    
    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotificationsByChannel(ChannelType channelType, Pageable pageable) {
        return notificationRepository.findByChannelTypeWithClient(channelType, pageable).map(this::toDto);
    }
    
    @Transactional
    public Optional<NotificationDto> retryNotification(UUID notificationId) {
        return notificationRepository.findById(notificationId)
                .filter(n -> n.getStatus() == NotificationStatus.FAILED || n.getStatus() == NotificationStatus.EXPIRED)
                .map(notification -> {
                    notification.setStatus(NotificationStatus.PENDING);
                    notification.setRetryCount(0);
                    notification.setNextRetryAt(null);
                    notification.setErrorMessage(null);
                    notification.setErrorCode(null);
                    notification.setExpiresAt(LocalDateTime.now().plusHours(24));
                    
                    notification = notificationRepository.save(notification);
                    
                    // Trigger send
                    if (notification.getChannelType() == ChannelType.EMAIL) {
                        emailService.sendEmail(notification);
                    }
                    
                    auditService.logNotificationStatusChange(
                            notificationId.toString(), "FAILED", "PENDING", "Manual retry triggered");
                    
                    return toDto(notification);
                });
    }
    
    public DashboardStatsDto getDashboardStats() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        
        Long totalSent = notificationRepository.countByStatusSince(NotificationStatus.SENT, since);
        Long totalFailed = notificationRepository.countByStatusSince(NotificationStatus.FAILED, since);
        Long totalPending = notificationRepository.countByStatusSince(NotificationStatus.PENDING, since);
        Long total = totalSent + totalFailed + totalPending;
        
        Double successRate = total > 0 ? (totalSent.doubleValue() / total) * 100 : 0.0;
        
        // By channel
        Map<String, Long> byChannel = notificationRepository.countByChannelSince(since)
                .stream()
                .collect(Collectors.toMap(
                        arr -> ((ChannelType) arr[0]).name(),
                        arr -> (Long) arr[1]
                ));
        
        // By status
        Map<String, Long> byStatus = notificationRepository.countByStatusGrouped(since)
                .stream()
                .collect(Collectors.toMap(
                        arr -> ((NotificationStatus) arr[0]).name(),
                        arr -> (Long) arr[1]
                ));
        
        // Hourly stats
        List<DashboardStatsDto.HourlyStatDto> hourlyStats = notificationRepository.countByHourSince(since)
                .stream()
                .map(arr -> DashboardStatsDto.HourlyStatDto.builder()
                        .hour((LocalDateTime) arr[0])
                        .count((Long) arr[1])
                        .build())
                .collect(Collectors.toList());
        
        // Recent errors
        List<NotificationDto> recentErrors = notificationRepository
                .findTop10ByStatusOrderByCreatedAtDesc(NotificationStatus.FAILED)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        
        return DashboardStatsDto.builder()
                .totalSent(totalSent)
                .totalFailed(totalFailed)
                .totalPending(totalPending)
                .successRate(Math.round(successRate * 100.0) / 100.0)
                .byChannel(byChannel)
                .byStatus(byStatus)
                .hourlyStats(hourlyStats)
                .recentErrors(recentErrors)
                .generatedAt(LocalDateTime.now())
                .build();
    }
    
    private NotificationDto toDto(Notification n) {
        return NotificationDto.builder()
                .notificationId(n.getNotificationId())
                .clientId(n.getClient() != null ? n.getClient().getClientId() : null)
                .clientName(n.getClient() != null ? n.getClient().getClientName() : "System")
                .channelType(n.getChannelType())
                .recipient(n.getRecipient())
                .subject(n.getSubject())
                .messageBody(n.getMessageBody())
                .status(n.getStatus())
                .priority(n.getPriority())
                .retryCount(n.getRetryCount())
                .maxRetries(n.getMaxRetries())
                .nextRetryAt(n.getNextRetryAt())
                .errorMessage(n.getErrorMessage())
                .errorCode(n.getErrorCode())
                .providerMessageId(n.getProviderMessageId())
                .idempotencyKey(n.getIdempotencyKey())
                .callbackUrl(n.getCallbackUrl())
                .metadata(n.getMetadata())
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .sentAt(n.getSentAt())
                .expiresAt(n.getExpiresAt())
                .build();
    }
    
    private String maskRecipient(String recipient) {
        if (recipient == null) return null;
        if (recipient.contains("@")) {
            int atIndex = recipient.indexOf("@");
            if (atIndex > 2) {
                return recipient.substring(0, 2) + "***" + recipient.substring(atIndex);
            }
        }
        if (recipient.length() > 4) {
            return recipient.substring(0, 2) + "***" + recipient.substring(recipient.length() - 2);
        }
        return "***";
    }
}
