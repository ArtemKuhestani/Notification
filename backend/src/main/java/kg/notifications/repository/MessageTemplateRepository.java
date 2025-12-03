package kg.notifications.repository;

import kg.notifications.entity.MessageTemplate;
import kg.notifications.entity.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, Integer> {
    
    Optional<MessageTemplate> findByTemplateCode(String templateCode);
    
    Optional<MessageTemplate> findByTemplateCodeAndIsActiveTrue(String templateCode);
    
    List<MessageTemplate> findByChannelTypeAndIsActiveTrue(ChannelType channelType);
    
    List<MessageTemplate> findByIsActiveTrue();
    
    boolean existsByTemplateCode(String templateCode);
}
