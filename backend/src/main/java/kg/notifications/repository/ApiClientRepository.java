package kg.notifications.repository;

import kg.notifications.entity.ApiClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiClientRepository extends JpaRepository<ApiClient, Integer> {
    
    Optional<ApiClient> findByClientName(String clientName);
    
    Optional<ApiClient> findByApiKeyHash(String apiKeyHash);
    
    Optional<ApiClient> findByApiKeyHashAndIsActiveTrue(String apiKeyHash);
    
    List<ApiClient> findByIsActiveTrue();
    
    boolean existsByClientName(String clientName);
    
    @Query("SELECT c FROM ApiClient c WHERE c.apiKeyPrefix = :prefix AND c.isActive = true")
    List<ApiClient> findByApiKeyPrefixAndActive(@Param("prefix") String prefix);
}
