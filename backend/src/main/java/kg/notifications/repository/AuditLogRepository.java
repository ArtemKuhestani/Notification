package kg.notifications.repository;

import kg.notifications.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    Page<AuditLog> findByAdminAdminId(Integer adminId, Pageable pageable);
    
    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);
    
    Page<AuditLog> findByActionType(String actionType, Pageable pageable);
    
    List<AuditLog> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime after);
    
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
