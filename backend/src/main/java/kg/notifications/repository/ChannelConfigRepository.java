package kg.notifications.repository;

import kg.notifications.entity.ChannelConfig;
import kg.notifications.entity.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelConfigRepository extends JpaRepository<ChannelConfig, Integer> {
    
    Optional<ChannelConfig> findByChannelName(ChannelType channelName);
    
    List<ChannelConfig> findByIsEnabledTrueOrderByPriorityAsc();
    
    Optional<ChannelConfig> findByChannelNameAndIsEnabledTrue(ChannelType channelName);
}
