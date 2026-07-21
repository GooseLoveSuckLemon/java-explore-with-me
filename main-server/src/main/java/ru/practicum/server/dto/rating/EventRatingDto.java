package ru.practicum.server.dto.rating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRatingDto {

    private Long id;

    private Long eventId;

    private Long userId;

    private Boolean isLike;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
