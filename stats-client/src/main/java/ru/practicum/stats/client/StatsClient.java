package ru.practicum.stats.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

    public void sendHit(EndpointHitDto hitDto) {
        try {
            String url = serverUrl + "/hit";
            HttpEntity<EndpointHitDto> requestEntity = new HttpEntity<>(hitDto, getDefaultHeaders());

            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Hit sent successfully: {}", hitDto);
            } else {
                log.error("Failed to send hit. Status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error sending hit: {}", e.getMessage(), e);
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats")
                    .queryParam("start", start.format(FORMATTER))
                    .queryParam("end", end.format(FORMATTER))
                    .queryParamIfPresent("uris", uris != null && !uris.isEmpty() ? java.util.Optional.of(uris) : null)
                    .queryParam("unique", unique)
                    .build()
                    .encode()
                    .toUriString();

            HttpEntity<Void> requestEntity = new HttpEntity<>(getDefaultHeaders());

            ResponseEntity<List<ViewStatsDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<List<ViewStatsDto>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Stats retrieved: {} records", response.getBody().size());
                return response.getBody();
            } else {
                log.warn("Failed to get stats. Status: {}", response.getStatusCode());
                return List.of();
            }
        } catch (Exception e) {
            log.error("Error getting stats: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private HttpHeaders getDefaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}