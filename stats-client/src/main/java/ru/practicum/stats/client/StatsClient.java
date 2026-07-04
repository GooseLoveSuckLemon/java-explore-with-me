package ru.practicum.stats.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatsClient {
    private final RestTemplate restTemplate;

    @Value("${stats-server.url:http://localhost:9090}")
    private String serverUrl;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void sendHit(EndpointHitDto hit) {
        try {
            String url = serverUrl + "/hit";
            restTemplate.postForObject(url, hit, Void.class);
            log.info("Запрос успешно отправлен: {}", hit);
        } catch (Exception e) {
            log.error("Ошибка отправки: {}", e.getMessage());
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats")
                    .queryParam("start", start.format(FORMATTER))
                    .queryParam("end", end.format(FORMATTER))
                    .queryParam("uris", String.join(",", uris))
                    .queryParam("unique", unique)
                    .toUriString();

            ResponseEntity<List<ViewStatsDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ViewStatsDto>>() {}
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Ошибка при получении статистики: {}", e.getMessage());
            return List.of();
        }
    }

    public Long getViewsForEvent(Long eventId) {
        try {
            LocalDateTime start = LocalDateTime.now().minusDays(30);
            LocalDateTime end = LocalDateTime.now().plusDays(1);
            List<String> uris = List.of("/events/" + eventId);

            // Используем unique=false, чтобы считать все просмотры
            List<ViewStatsDto> stats = getStats(start, end, uris, false);

            if (stats != null && !stats.isEmpty()) {
                return stats.get(0).getHits();
            }
            return 0L;
        } catch (Exception e) {
            log.error("Ошибка при получении данных о просмотрах события {}: {}", eventId, e.getMessage());
            return 0L;
        }
    }
}