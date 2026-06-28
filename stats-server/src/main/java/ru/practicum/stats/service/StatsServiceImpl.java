package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
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
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        List<Object[]> results;
        if (unique) {
            results = statsRepository.findUniqueStats(start, end, uris);
        } else {
            results = statsRepository.findStats(start, end, uris);
        }

        return results.stream()
                .map(row -> ViewStatsDto.builder()
                        .app((String) row[APP_INDEX])
                        .uri((String) row[URI_INDEX])
                        .hits((Long) row[HITS_INDEX])
                        .build())
                .collect(Collectors.toList());
    }
}