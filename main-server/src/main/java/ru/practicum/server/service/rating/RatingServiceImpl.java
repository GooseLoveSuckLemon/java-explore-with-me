package ru.practicum.server.service.rating;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.server.dto.rating.EventRatingDto;
import ru.practicum.server.dto.rating.EventRatingStatsDto;
import ru.practicum.server.dto.rating.UserRatingDto;
import ru.practicum.server.exception.ConflictException;
import ru.practicum.server.exception.NotFoundException;
import ru.practicum.server.mapper.rating.RatingMapper;
import ru.practicum.server.model.event.Event;
import ru.practicum.server.model.event.EventState;
import ru.practicum.server.model.rating.EventRating;
import ru.practicum.server.model.user.User;
import ru.practicum.server.repository.event.EventRepository;
import ru.practicum.server.repository.rating.EventRatingRepository;
import ru.practicum.server.repository.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingServiceImpl implements RatingService {

    private final EventRatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public EventRatingDto addOrUpdateRating(Long userId, Long eventId, Boolean isLike) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Нельзя оценивать собственное событие");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Можно оценивать только опубликованные события");
        }

        Optional<EventRating> existingRating = ratingRepository.findByEventIdAndUserId(eventId, userId);

        EventRating rating;
        if (existingRating.isPresent()) {
            rating = existingRating.get();
            rating.setIsLike(isLike);
            rating.setUpdatedAt(LocalDateTime.now());
            log.info("Обновлена оценка для события {} пользователем {}: {}", eventId, userId, isLike);
        } else {
            rating = EventRating.builder()
                    .event(event)
                    .user(user)
                    .isLike(isLike)
                    .createdAt(LocalDateTime.now())
                    .build();
            log.info("Добавлена оценка для события {} пользователем {}: {}", eventId, userId, isLike);
        }

        rating = ratingRepository.save(rating);
        return RatingMapper.toDto(rating);
    }

    @Override
    @Transactional
    public void deleteRating(Long userId, Long eventId) {
        EventRating rating = ratingRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Оценка для события " + eventId + " от пользователя " + userId + " не найдена"));

        ratingRepository.delete(rating);
        log.info("Удалена оценка для события {} от пользователя {}", eventId, userId);
    }

    @Override
    public EventRatingDto getRating(Long userId, Long eventId) {
        EventRating rating = ratingRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Оценка для события " + eventId + " от пользователя " + userId + " не найдена"));
        return RatingMapper.toDto(rating);
    }

    @Override
    public EventRatingStatsDto getEventRatingStats(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие с ID " + eventId + " не найдено");
        }

        long likes = ratingRepository.countLikesByEventId(eventId);
        long dislikes = ratingRepository.countDislikesByEventId(eventId);

        return RatingMapper.toStatsDto(eventId, likes, dislikes);
    }

    @Override
    public Map<Long, EventRatingStatsDto> getEventRatingStats(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Object[]> likesResults = ratingRepository.countLikesByEventIds(eventIds);
        Map<Long, Long> likesMap = likesResults.stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));

        List<Object[]> dislikesResults = ratingRepository.countDislikesByEventIds(eventIds);
        Map<Long, Long> dislikesMap = dislikesResults.stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));

        return eventIds.stream()
                .collect(Collectors.toMap(
                        eventId -> eventId,
                        eventId -> {
                            Long likes = likesMap.get(eventId);
                            Long dislikes = dislikesMap.get(eventId);
                            return RatingMapper.toStatsDto(eventId, likes, dislikes);
                        }
                ));
    }

    @Override
    public UserRatingDto getUserRating(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        long totalEvents = eventRepository.countByInitiatorId(userId);
        Long totalLikes = getTotalLikesForUser(userId);
        Long totalDislikes = getTotalDislikesForUser(userId);

        return RatingMapper.toUserRatingDto(
                userId,
                user.getName(),
                totalEvents,
                totalLikes,
                totalDislikes
        );
    }

    @Override
    public Map<Long, UserRatingDto> getUserRatings(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<User> users = userRepository.findByIdIn(userIds);

        List<Object[]> likesResults = ratingRepository.countLikesByUserIds(userIds);
        Map<Long, Long> likesMap = likesResults.stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));

        List<Object[]> dislikesResults = ratingRepository.countDislikesByUserIds(userIds);
        Map<Long, Long> dislikesMap = dislikesResults.stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));

        Map<Long, Long> eventsCountMap = eventRepository.countEventsByInitiatorIds(userIds);

        return users.stream()
                .collect(Collectors.toMap(
                        User::getId,
                        user -> {
                            long totalEvents = eventsCountMap.getOrDefault(user.getId(), 0L);
                            long totalLikes = likesMap.getOrDefault(user.getId(), 0L);
                            long totalDislikes = dislikesMap.getOrDefault(user.getId(), 0L);
                            return RatingMapper.toUserRatingDto(
                                    user.getId(),
                                    user.getName(),
                                    totalEvents,
                                    totalLikes,
                                    totalDislikes
                            );
                        }
                ));
    }

    private Long getTotalLikesForUser(Long userId) {
        List<Object[]> results = ratingRepository.countLikesByUserIds(List.of(userId));
        return results.isEmpty() ? 0L : (Long) results.get(0)[1];
    }

    private Long getTotalDislikesForUser(Long userId) {
        List<Object[]> results = ratingRepository.countDislikesByUserIds(List.of(userId));
        return results.isEmpty() ? 0L : (Long) results.get(0)[1];
    }
}