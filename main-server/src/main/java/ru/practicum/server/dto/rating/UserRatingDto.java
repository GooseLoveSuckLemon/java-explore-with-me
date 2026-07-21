package ru.practicum.server.dto.rating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRatingDto {

    private Long userId;

    private String userName;

    private Long totalEvents;

    private Long totalLikes;

    private Long totalDislikes;

    private Double averageRating;
}
