package practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import practicum.dto.EndpointHitDto;
import practicum.dto.ViewStatsDto;
import practicum.mapper.EndpointHitMapper;
import practicum.model.EndpointHit;
import practicum.repository.StatsRepository;
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
                        .app((String) row[0])
                        .uri((String) row[1])
                        .hits((Long) row[2])
                        .build())
                .collect(Collectors.toList());
    }
}