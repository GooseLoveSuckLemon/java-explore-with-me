package ru.practicum.explore.service.event;

import ru.practicum.explore.dto.event.*;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    List<EventFullDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto getUserEvent(Long userId, Long eventId);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request);


    List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request);

    List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        Boolean onlyAvailable, String sort, Integer from, Integer size);

    EventFullDto getPublicEvent(Long eventId);

    void deleteEvent(Long eventId);
}