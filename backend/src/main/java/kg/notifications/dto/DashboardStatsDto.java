package kg.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDto {
    
    private Long totalSent;
    private Long totalFailed;
    private Long totalPending;
    private Double successRate;
    private Map<String, Long> byChannel;
    private Map<String, Long> byStatus;
    private List<HourlyStatDto> hourlyStats;
    private List<NotificationDto> recentErrors;
    private LocalDateTime generatedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HourlyStatDto {
        private LocalDateTime hour;
        private Long count;
    }
}
