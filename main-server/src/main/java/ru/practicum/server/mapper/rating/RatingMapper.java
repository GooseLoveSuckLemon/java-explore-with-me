package ru.practicum.server.mapper.rating;

import ru.practicum.server.dto.rating.EventRatingDto;
import ru.practicum.server.dto.rating.EventRatingStatsDto;
import ru.practicum.server.dto.rating.UserRatingDto;
import ru.practicum.server.model.rating.EventRating;

public class RatingMapper {

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

    public static EventRatingStatsDto toStatsDto(Long eventId, Long likes, Long dislikes) {

        long likesValue = likes != null ? likes : 0L;

        long dislikesValue = dislikes != null ? dislikes : 0L;

        long totalVotes = likesValue + dislikesValue;

        Double rating = totalVotes > 0 ? (double) likesValue / totalVotes * 100 : 0.0;

        return EventRatingStatsDto.builder()
                .eventId(eventId)
                .likes(likesValue)
                .dislikes(dislikesValue)
                .rating(rating)
                .totalVotes(totalVotes)
                .build();
    }

    public static UserRatingDto toUserRatingDto(Long userId, String userName,
                                                Long totalEvents, Long totalLikes,
                                                Long totalDislikes) {

        long events = totalEvents != null ? totalEvents : 0L;

        long likes = totalLikes != null ? totalLikes : 0L;

        long dislikes = totalDislikes != null ? totalDislikes : 0L;

        long totalVotes = likes + dislikes;

        Double averageRating = events > 0 && totalVotes > 0
                ? (double) likes / (totalVotes) * 100 : 0.0;

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
