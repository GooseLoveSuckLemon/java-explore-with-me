package ru.practicum.server.service.rating;

import ru.practicum.server.dto.rating.EventRatingDto;
import ru.practicum.server.dto.rating.EventRatingStatsDto;
import ru.practicum.server.dto.rating.UserRatingDto;

import java.util.List;
import java.util.Map;

public interface RatingService {

    /**
     * Добавляет или обновляет оценку пользователя для события
     */
    EventRatingDto addOrUpdateRating(Long userId, Long eventId, Boolean isLike);

    /**
     * Удаляет оценку пользователя для события
     */
    void deleteRating(Long userId, Long eventId);

    /**
     * Получает оценку пользователя для события
     */
    EventRatingDto getRating(Long userId, Long eventId);

    /**
     * Получает статистику рейтинга для события
     */
    EventRatingStatsDto getEventRatingStats(Long eventId);

    /**
     * Получает статистику рейтингов для нескольких событий
     */
    Map<Long, EventRatingStatsDto> getEventRatingStats(List<Long> eventIds);

    /**
     * Получает рейтинг пользователя (автора событий)
     */
    UserRatingDto getUserRating(Long userId);

    /**
     * Получает рейтинги нескольких пользователей
     */
    Map<Long, UserRatingDto> getUserRatings(List<Long> userIds);
}