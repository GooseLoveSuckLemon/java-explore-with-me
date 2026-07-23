package ru.practicum.server.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.server.dto.category.CategoryDto;
import ru.practicum.server.dto.location.EventLocationDto;
import ru.practicum.server.dto.rating.EventRatingStatsDto;
import ru.practicum.server.dto.user.UserShortDto;

import java.time.LocalDateTime;

import static ru.practicum.server.util.Constants.DATE_TIME_PATTERN;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {

    private Long id;

    private String annotation;

    private CategoryDto category;

    private Long confirmedRequests;

    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime createdOn;

    private String description;

    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime eventDate;

    private UserShortDto initiator;

    private EventLocationDto location;

    private Boolean paid;

    private Integer participantLimit;

    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime publishedOn;

    private Boolean requestModeration;

    private String state;

    private String title;

    private Long views;

    private EventRatingStatsDto rating;
}