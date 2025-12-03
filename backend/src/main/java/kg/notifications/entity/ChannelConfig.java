package kg.notifications.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "channel_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Integer configId;

    @Column(name = "channel_name", nullable = false, unique = true, length = 20)
    @Enumerated(EnumType.STRING)
    private ChannelType channelName;

    @Column(name = "provider_name", nullable = false, length = 50)
    private String providerName;

    @Column(name = "credentials", nullable = false, columnDefinition = "bytea")
    private byte[] credentials;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "settings", columnDefinition = "jsonb")
    private Map<String, Object> settings;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = false;

    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    @Column(name = "daily_limit")
    private Integer dailyLimit;

    @Column(name = "daily_sent_count", nullable = false)
    private Integer dailySentCount = 0;

    @Column(name = "last_health_check")
    private LocalDateTime lastHealthCheck;

    @Column(name = "health_status", length = 20)
    @Enumerated(EnumType.STRING)
    private HealthStatus healthStatus = HealthStatus.UNKNOWN;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum HealthStatus {
        UNKNOWN, HEALTHY, UNHEALTHY, DEGRADED
    }
}
