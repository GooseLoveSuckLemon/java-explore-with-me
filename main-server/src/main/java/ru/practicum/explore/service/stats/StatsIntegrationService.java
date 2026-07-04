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
            log.info("Sending hit: {}", hit);
            statsClient.sendHit(hit);
        } catch (Exception e) {
            log.error("Error sending hit to stats service: {}", e.getMessage(), e);
        }
    }

    public Long getViewsForEvent(Long eventId) {
        try {
            LocalDateTime start = LocalDateTime.now().minusYears(1);
            LocalDateTime end = LocalDateTime.now().plusDays(1);
            String uri = "/events/" + eventId;

            log.info("Getting views for event {}: start={}, end={}, uri={}", eventId, start, end, uri);

            // Пробуем получить статистику несколько раз
            List<ViewStatsDto> stats = null;
            for (int i = 0; i < 3; i++) {
                stats = statsClient.getStats(start, end, List.of(uri), false);
                if (stats != null && !stats.isEmpty()) {
                    break;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            log.info("Stats response for event {}: {}", eventId, stats);

            if (stats != null && !stats.isEmpty()) {
                return stats.get(0).getHits();
            }
            return 0L;
        } catch (Exception e) {
            log.error("Error getting views for event {}: {}", eventId, e.getMessage(), e);
            return 0L;
        }
    }
}