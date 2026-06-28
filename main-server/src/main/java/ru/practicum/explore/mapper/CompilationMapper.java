package ru.practicum.explore.mapper;

import ru.practicum.explore.dto.compilation.CompilationDto;
import ru.practicum.explore.dto.event.EventShortDto;
import ru.practicum.explore.model.compilation.Compilation;

import java.util.List;
import java.util.stream.Collectors;

public class CompilationMapper {

    public static CompilationDto toDto(Compilation compilation) {
        List<EventShortDto> events = compilation.getEvents() != null ?
                compilation.getEvents().stream()
                        .map(event -> EventMapper.toShortDto(event, 0L, 0L))
                        .collect(Collectors.toList()) :
                List.of();

        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(events)
                .build();
    }
}