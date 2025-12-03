package kg.notifications.repository;

import kg.notifications.entity.RetryQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RetryQueueRepository extends JpaRepository<RetryQueue, Long> {
    
    @Query("SELECT r FROM RetryQueue r WHERE r.status = 'PENDING' AND r.scheduledAt <= :now ORDER BY r.scheduledAt")
    List<RetryQueue> findPendingRetries(@Param("now") LocalDateTime now);
    
    List<RetryQueue> findByNotificationNotificationId(UUID notificationId);
    
    Long countByStatus(RetryQueue.RetryStatus status);
}
