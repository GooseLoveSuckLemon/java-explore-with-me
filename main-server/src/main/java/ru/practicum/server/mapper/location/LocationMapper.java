package ru.practicum.server.mapper.location;

import ru.practicum.server.dto.location.EventLocationDto;
import ru.practicum.server.model.location.EventLocation;

public class LocationMapper {

    public static EventLocation toEventLocation(EventLocationDto dto) {
        if (dto == null) return null;
        return EventLocation.builder()
                .latitude(dto.getLat())
                .longitude(dto.getLon())
                .build();
    }

    public static EventLocationDto toLocationDto(EventLocation location) {
        if (location == null) return null;
        return EventLocationDto.builder()
                .lat(location.getLatitude())
                .lon(location.getLongitude())
                .build();
    }
}
