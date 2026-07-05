package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.mapper.EndpointHitMapper;
import ru.practicum.stats.model.EndpointHit;
import ru.practicum.stats.repository.StatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    private static final int APP_INDEX = 0;

    private static final int URI_INDEX = 1;

    private static final int HITS_INDEX = 2;

    @Override
    @Transactional
    public void saveHit(EndpointHitDto endpointHitDto) {
        EndpointHit hit = EndpointHitMapper.toEntity(endpointHitDto);
        statsRepository.save(hit);
        log.info("Saved hit: {}", hit);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end dates are required");
        }

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        List<Object[]> results;
        if (Boolean.TRUE.equals(unique)) {
            results = statsRepository.findUniqueStats(start, end, uris);
        } else {
            results = statsRepository.findStats(start, end, uris);
        }

        return results.stream()
                .map(row -> ViewStatsDto.builder()
                        .app(row[APP_INDEX] != null ? row[APP_INDEX].toString() : "")
                        .uri(row[URI_INDEX] != null ? row[URI_INDEX].toString() : "")
                        .hits(row[HITS_INDEX] instanceof Number ? ((Number) row[HITS_INDEX]).longValue() : 0L)
                        .build())
                .collect(Collectors.toList());
    }
}