package ru.practicum.server.mapper.rating;

import ru.practicum.server.dto.rating.EventRatingDto;
import ru.practicum.server.dto.rating.EventRatingStatsDto;
import ru.practicum.server.dto.rating.UserRatingDto;
import ru.practicum.server.model.rating.EventRating;

import static ru.practicum.server.util.Constants.DEFAULT_RATING;
import static ru.practicum.server.util.Constants.PERCENTAGE_MULTIPLIER;

public class RatingMapper {

    /**
     * Преобразует сущность {@link EventRating} в DTO {@link EventRatingDto}.
     *
     * @param rating сущность оценки
     * @return DTO оценки
     */
    public static EventRatingDto toDto(EventRating rating) {
        return EventRatingDto.builder()
                .id(rating.getId())
                .eventId(rating.getEvent().getId())
                .userId(rating.getUser().getId())
                .isLike(rating.getIsLike())
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }

    /**
     * Преобразует статистику рейтинга события в DTO {@link EventRatingStatsDto}.
     * Рассчитывает рейтинг как процент лайков от общего числа голосов.
     *
     * @param eventId идентификатор события
     * @param likes   количество лайков
     * @param dislikes количество дизлайков
     * @return DTO со статистикой рейтинга
     */
    public static EventRatingStatsDto toStatsDto(Long eventId, Long likes, Long dislikes) {

        long likesValue = likes != null ? likes : 0L;

        long dislikesValue = dislikes != null ? dislikes : 0L;

        long totalVotes = likesValue + dislikesValue;

        Double rating = totalVotes > 0 ? (double) likesValue / totalVotes * PERCENTAGE_MULTIPLIER : DEFAULT_RATING;

        return EventRatingStatsDto.builder()
                .eventId(eventId)
                .likes(likesValue)
                .dislikes(dislikesValue)
                .rating(rating)
                .totalVotes(totalVotes)
                .build();
    }

    /**
     * Преобразует статистику рейтинга пользователя как автора в DTO {@link UserRatingDto}.
     * Рассчитывает средний рейтинг как процент лайков от общего числа голосов.
     *
     * @param userId        идентификатор пользователя
     * @param userName      имя пользователя
     * @param totalEvents   общее количество событий пользователя
     * @param totalLikes    общее количество лайков на всех событиях пользователя
     * @param totalDislikes общее количество дизлайков на всех событиях пользователя
     * @return DTO с рейтингом пользователя
     */
    public static UserRatingDto toUserRatingDto(Long userId, String userName,
                                                Long totalEvents, Long totalLikes,
                                                Long totalDislikes) {

        long events = totalEvents != null ? totalEvents : 0L;

        long likes = totalLikes != null ? totalLikes : 0L;

        long dislikes = totalDislikes != null ? totalDislikes : 0L;

        long totalVotes = likes + dislikes;

        Double averageRating = events > 0 && totalVotes > 0
                ? (double) likes / (totalVotes) * PERCENTAGE_MULTIPLIER : DEFAULT_RATING;

        return UserRatingDto.builder()
                .userId(userId)
                .userName(userName)
                .totalEvents(events)
                .totalLikes(likes)
                .totalDislikes(dislikes)
                .averageRating(averageRating)
                .build();
    }
}
