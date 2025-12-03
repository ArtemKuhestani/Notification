package kg.notifications.repository;

import kg.notifications.entity.ChannelType;
import kg.notifications.entity.Notification;
import kg.notifications.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID>, JpaSpecificationExecutor<Notification> {
    
    Optional<Notification> findByIdempotencyKey(String idempotencyKey);
    
    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.client WHERE n.status = :status")
    Page<Notification> findByStatusWithClient(@Param("status") NotificationStatus status, Pageable pageable);
    
    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.client WHERE n.channelType = :channelType")
    Page<Notification> findByChannelTypeWithClient(@Param("channelType") ChannelType channelType, Pageable pageable);
    
    Page<Notification> findByClientClientId(Integer clientId, Pageable pageable);
    
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.nextRetryAt <= :now")
    List<Notification> findPendingRetries(
            @Param("status") NotificationStatus status,
            @Param("now") LocalDateTime now);
    
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND n.expiresAt < :now")
    List<Notification> findExpiredNotifications(@Param("now") LocalDateTime now);
    
    // Statistics queries
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.createdAt >= :since")
    Long countTotalSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = :status AND n.createdAt >= :since")
    Long countByStatusSince(@Param("status") NotificationStatus status, @Param("since") LocalDateTime since);
    
    @Query("SELECT n.channelType, COUNT(n) FROM Notification n WHERE n.createdAt >= :since GROUP BY n.channelType")
    List<Object[]> countByChannelSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT n.status, COUNT(n) FROM Notification n WHERE n.createdAt >= :since GROUP BY n.status")
    List<Object[]> countByStatusGrouped(@Param("since") LocalDateTime since);
    
    @Query("SELECT FUNCTION('date_trunc', 'hour', n.createdAt) as hour, COUNT(n) " +
           "FROM Notification n WHERE n.createdAt >= :since " +
           "GROUP BY FUNCTION('date_trunc', 'hour', n.createdAt) " +
           "ORDER BY hour")
    List<Object[]> countByHourSince(@Param("since") LocalDateTime since);
    
    List<Notification> findTop10ByStatusOrderByCreatedAtDesc(NotificationStatus status);
    
    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.client ORDER BY n.createdAt DESC")
    Page<Notification> findAllWithClient(Pageable pageable);
}
