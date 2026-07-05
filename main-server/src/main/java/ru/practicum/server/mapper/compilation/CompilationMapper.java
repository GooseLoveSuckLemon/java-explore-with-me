package ru.practicum.server.mapper.compilation;

import jdk.jfr.Event;
import ru.practicum.server.dto.compilation.CompilationDto;
import ru.practicum.server.dto.event.EventShortDto;
import ru.practicum.server.mapper.event.EventMapper;
import ru.practicum.server.model.compilation.Compilation;

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
