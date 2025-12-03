package kg.notifications.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "api_clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id")
    private Integer clientId;

    @Column(name = "client_name", nullable = false, unique = true)
    private String clientName;

    @Column(name = "client_description")
    private String clientDescription;

    @Column(name = "api_key_hash", nullable = false, length = 64)
    private String apiKeyHash;

    @Column(name = "api_key_prefix", nullable = false, length = 8)
    private String apiKeyPrefix;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "rate_limit", nullable = false)
    private Integer rateLimit = 100;

    @Column(name = "allowed_channels", columnDefinition = "varchar[]")
    private String[] allowedChannels;

    @Column(name = "allowed_ips", columnDefinition = "inet[]")
    private String[] allowedIps;

    @Column(name = "callback_url_default", length = 500)
    private String callbackUrlDefault;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Admin createdBy;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    private List<Notification> notifications;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
