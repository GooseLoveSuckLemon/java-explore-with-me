package ru.practicum.server.dto.rating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRatingStatsDto {

    private Long eventId;

    private long likes;

    private long dislikes;

    private long totalLikes;

    private Double rating;

    private long totalVotes;
}
