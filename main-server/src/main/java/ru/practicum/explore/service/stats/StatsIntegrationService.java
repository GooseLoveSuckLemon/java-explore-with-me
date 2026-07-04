package ru.practicum.explore.service.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.stats.client.StatsClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsIntegrationService {
    private final StatsClient statsClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void sendHit(String app, String uri, String ip, LocalDateTime timestamp) {
        try {
            EndpointHitDto hit = EndpointHitDto.builder()
                    .app(app)
                    .uri(uri)
                    .ip(ip)
                    .timestamp(timestamp)
                    .build();
            statsClient.sendHit(hit);
            log.info("Sent hit to stats service: {}", hit);
        } catch (Exception e) {
            log.error("Error sending hit to stats service: {}", e.getMessage());
        }
    }

    public Long getViewsForEvent(Long eventId) {
        try {
            LocalDateTime start = LocalDateTime.now().minusYears(1);
            LocalDateTime end = LocalDateTime.now();
            String uri = "/events/" + eventId;
            List<ViewStatsDto> stats = statsClient.getStats(start, end, List.of(uri), false);
            return stats.isEmpty() ? 0L : stats.get(0).getHits();
        } catch (Exception e) {
            log.error("Error getting views for event {}: {}", eventId, e.getMessage());
            return 0L;
        }
    }
}