package ru.practicum.server.service.event;

import ru.practicum.server.dto.event.EventDto;
import ru.practicum.server.dto.event.EventShortDto;
import ru.practicum.server.dto.event.NewEventDto;
import ru.practicum.server.dto.event.update.UpdateEventAdminRequest;
import ru.practicum.server.dto.event.update.UpdateEventUserRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    EventDto createEvent(Long userId, NewEventDto newEventDto);

    List<EventDto> getUserEvents(Long userId, Integer from, Integer size);

    EventDto getUserEvent(Long userId, Long eventId);

    EventDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request);

    List<EventDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                    LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request);

    List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        Boolean onlyAvailable, String sort, Integer from, Integer size);

    EventDto getPublicEvent(Long eventId);

    void deleteEvent(Long eventId);
}
