package ru.practicum.explore.mapper;

import ru.practicum.explore.dto.location.LocationDto;
import ru.practicum.explore.model.location.EventLocation;

public class LocationMapper {

    public static EventLocation toEventLocation(LocationDto dto) {
        if (dto == null) return null;
        return EventLocation.builder()
                .lat(dto.getLat())
                .lon(dto.getLon())
                .build();
    }

    public static LocationDto toLocationDto(EventLocation location) {
        if (location == null) return null;
        return LocationDto.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }
}