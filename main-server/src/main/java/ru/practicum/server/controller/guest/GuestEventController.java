package ru.practicum.server.controller.guest;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.server.controller.BaseController;
import ru.practicum.server.dto.event.EventDto;
import ru.practicum.server.dto.event.EventShortDto;
import ru.practicum.server.service.event.EventService;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(value = {"/events", "/events/"})
@RequiredArgsConstructor
@Slf4j
public class GuestEventController extends BaseController {

    private final EventService eventService;
    private final StatsClient statsClient;

    @GetMapping
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        return eventService.getPublicEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }

    @GetMapping("/{id}")
    public EventDto getEvent(@PathVariable Long id, HttpServletRequest request) {

        String ip = request.getRemoteAddr();

        statsClient.sendHit(
                EndpointHitDto.builder()
                        .app("main-service")
                        .uri("/events/" + id)
                        .ip(ip)
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        EventDto eventDto = eventService.getPublicEvent(id);
        eventDto.setViews(eventDto.getViews() + 1);
        return eventDto;
    }
}
