package kg.notifications.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import kg.notifications.entity.ChannelConfig;
import kg.notifications.entity.ChannelType;
import kg.notifications.entity.Notification;
import kg.notifications.entity.NotificationStatus;
import kg.notifications.repository.ChannelConfigRepository;
import kg.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;
    private final ChannelConfigRepository channelConfigRepository;
    private final AuditService auditService;
    
    @Async
    @Transactional
    public CompletableFuture<Boolean> sendEmail(Notification notification) {
        log.info("Sending email to: {}, subject: {}", 
                maskEmail(notification.getRecipient()), notification.getSubject());
        
        // Update status to SENDING
        notification.setStatus(NotificationStatus.SENDING);
        notificationRepository.save(notification);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(notification.getRecipient());
            helper.setSubject(notification.getSubject() != null ? notification.getSubject() : "Notification");
            helper.setText(notification.getMessageBody(), isHtml(notification.getMessageBody()));
            
            // Get sender email from config
            String fromEmail = getFromEmail();
            if (fromEmail != null) {
                helper.setFrom(fromEmail);
            }
            
            mailSender.send(message);
            
            // Update status to SENT
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setErrorMessage(null);
            notification.setErrorCode(null);
            notificationRepository.save(notification);
            
            // Log success
            auditService.logNotificationStatusChange(
                    notification.getNotificationId().toString(),
                    "SENDING", "SENT", null);
            
            log.info("Email sent successfully to: {}", maskEmail(notification.getRecipient()));
            return CompletableFuture.completedFuture(true);
            
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", maskEmail(notification.getRecipient()), e.getMessage());
            
            handleSendError(notification, e.getMessage(), "MESSAGING_ERROR");
            return CompletableFuture.completedFuture(false);
        } catch (Exception e) {
            log.error("Unexpected error sending email to {}: {}", 
                    maskEmail(notification.getRecipient()), e.getMessage(), e);
            
            handleSendError(notification, e.getMessage(), "UNKNOWN_ERROR");
            return CompletableFuture.completedFuture(false);
        }
    }
    
    private void handleSendError(Notification notification, String errorMessage, String errorCode) {
        notification.setRetryCount(notification.getRetryCount() + 1);
        notification.setErrorMessage(errorMessage);
        notification.setErrorCode(errorCode);
        
        if (notification.getRetryCount() >= notification.getMaxRetries()) {
            notification.setStatus(NotificationStatus.FAILED);
            log.warn("Max retries reached for notification {}", notification.getNotificationId());
        } else {
            notification.setStatus(NotificationStatus.PENDING);
            notification.setNextRetryAt(calculateNextRetry(notification.getRetryCount()));
            log.info("Scheduled retry {} for notification {} at {}", 
                    notification.getRetryCount(), notification.getNotificationId(), notification.getNextRetryAt());
        }
        
        notificationRepository.save(notification);
        
        auditService.logNotificationStatusChange(
                notification.getNotificationId().toString(),
                "SENDING", notification.getStatus().name(), errorMessage);
    }
    
    private LocalDateTime calculateNextRetry(int retryCount) {
        // Exponential backoff: 1min, 5min, 15min, 1hr, 4hr
        int[] intervals = {1, 5, 15, 60, 240};
        int index = Math.min(retryCount - 1, intervals.length - 1);
        return LocalDateTime.now().plusMinutes(intervals[index]);
    }
    
    private boolean isHtml(String content) {
        return content != null && (content.contains("<html") || content.contains("<body") 
                || content.contains("<p>") || content.contains("<div"));
    }
    
    private String getFromEmail() {
        try {
            return channelConfigRepository.findByChannelName(ChannelType.EMAIL)
                    .map(config -> {
                        Map<String, Object> settings = config.getSettings();
                        if (settings != null && settings.containsKey("from_email")) {
                            return (String) settings.get("from_email");
                        }
                        return null;
                    })
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Could not get from email from config: {}", e.getMessage());
            return null;
        }
    }
    
    private String maskEmail(String email) {
        if (email == null) return null;
        int atIndex = email.indexOf("@");
        if (atIndex > 2) {
            return email.substring(0, 2) + "***" + email.substring(atIndex);
        }
        return "***@***";
    }
    
    /**
     * Test email connection
     */
    public boolean testConnection() {
        try {
            if (mailSender instanceof JavaMailSenderImpl) {
                ((JavaMailSenderImpl) mailSender).testConnection();
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Email connection test failed: {}", e.getMessage());
            return false;
        }
    }
}
