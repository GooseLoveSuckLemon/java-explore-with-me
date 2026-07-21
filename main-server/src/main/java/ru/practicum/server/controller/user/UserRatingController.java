package ru.practicum.server.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.server.controller.BaseController;
import ru.practicum.server.dto.rating.EventRatingDto;
import ru.practicum.server.dto.rating.EventRatingStatsDto;
import ru.practicum.server.dto.rating.UserRatingDto;
import ru.practicum.server.service.rating.RatingService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users/{userId}/ratings")
@RequiredArgsConstructor
public class UserRatingController extends BaseController {

    private final RatingService ratingService;


    /**
     * Получение рейтинга пользователя как автора событий.
     */
    @GetMapping("/user-rating")
    public UserRatingDto getUserRating(@PathVariable Long userId) {
        return ratingService.getUserRating(userId);
    }

    /**
     * Получение рейтингов нескольких пользователей.
     */
    @GetMapping("/user-ratings")
    public Map<Long, UserRatingDto> getUserRatings(@RequestParam List<Long> userIds) {
        return ratingService.getUserRatings(userIds);
    }

    /**
     * Получение оценки пользователя для события.
     * Должен идти ПОСЛЕ конкретных путей (/user-rating, /user-ratings)
     */
    @GetMapping("/{eventId}")
    public EventRatingDto getRating(@PathVariable Long userId, @PathVariable Long eventId) {
        return ratingService.getRating(userId, eventId);
    }

    /**
     * Получение статистики рейтинга события.
     */
    @GetMapping("/{eventId}/stats")
    public EventRatingStatsDto getEventRatingStats(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        return ratingService.getEventRatingStats(eventId);
    }

    /**
     * Добавление или обновление оценки события.
     */
    @PostMapping("/{eventId}")
    public EventRatingDto addOrUpdateRating(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestParam Boolean isLike) {
        return ratingService.addOrUpdateRating(userId, eventId, isLike);
    }

    /**
     * Удаление оценки события.
     */
    @DeleteMapping("/{eventId}")
    public void deleteRating(@PathVariable Long userId, @PathVariable Long eventId) {
        ratingService.deleteRating(userId, eventId);
    }
}