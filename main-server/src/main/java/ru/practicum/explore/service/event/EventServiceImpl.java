package ru.practicum.explore.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.dto.event.*;
import ru.practicum.explore.exception.ConflictException;
import ru.practicum.explore.exception.NotFoundException;
import ru.practicum.explore.mapper.EventMapper;
import ru.practicum.explore.model.category.Category;
import ru.practicum.explore.model.event.Event;
import ru.practicum.explore.model.event.EventState;
import ru.practicum.explore.model.participationRequest.RequestStatus;
import ru.practicum.explore.model.user.User;
import ru.practicum.explore.repository.category.CategoryRepository;
import ru.practicum.explore.repository.event.EventRepository;
import ru.practicum.explore.repository.participation.ParticipationRequestRepository;
import ru.practicum.explore.repository.user.UserRepository;
import ru.practicum.explore.service.stats.StatsIntegrationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository requestRepository;
    private final StatsIntegrationService statsIntegrationService;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с Id " + newEventDto.getCategory() + " не найдена"));

        LocalDateTime now = LocalDateTime.now();
        if (newEventDto.getEventDate().isBefore(now.plusHours(2))) {
            throw new ConflictException("Дата события должна быть не ранее чем через 2 часа от текущего момента.");
        }

        Event event = EventMapper.toEntity(newEventDto, user, category);
        event.setCreatedOn(now);
        event.setState(EventState.PENDING);

        event = eventRepository.save(event);
        Long confirmedRequests = 0L;
        Long views = 0L;
        return EventMapper.toFullDto(event, confirmedRequests, views);
    }

    @Override
    public List<EventFullDto> getUserEvents(Long userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        return eventRepository.findByInitiatorId(userId, pageable).stream()
                .map(event -> {
                    Long confirmedRequests = getConfirmedRequests(event.getId());
                    Long views = statsIntegrationService.getViewsForEvent(event.getId());
                    return EventMapper.toFullDto(event, confirmedRequests, views);
                })
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Ивент с ID " + eventId + " не найден"));

        Long confirmedRequests = getConfirmedRequests(event.getId());
        Long views = statsIntegrationService.getViewsForEvent(event.getId());
        return EventMapper.toFullDto(event, confirmedRequests, views);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Ивент с ID " + eventId + " не найден"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Изменить можно только ожидающие или отмененные события.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (request.getEventDate() != null && request.getEventDate().isBefore(now.plusHours(2))) {
            throw new ConflictException("Дата события должна быть не ранее чем через 2 часа от текущего момента.");
        }

        Category category = null;
        if (request.getCategory() != null) {
            category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
        }

        EventMapper.applyUserUpdate(event, request, category);

        event = eventRepository.save(event);
        Long confirmedRequests = getConfirmedRequests(event.getId());
        Long views = statsIntegrationService.getViewsForEvent(event.getId());
        return EventMapper.toFullDto(event, confirmedRequests, views);
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> users,
                                               List<String> states,
                                               List<Long> categories,
                                               LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd,
                                               Integer from,
                                               Integer size) {

        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new ConflictException("Дата окончания не может быть раньше даты начала.");
        }

        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.searchAdmin(users, states, categories, rangeStart, rangeEnd, pageable);

        return events.stream()
                .map(event -> {
                    Long confirmedRequests = getConfirmedRequests(event.getId());
                    Long views = statsIntegrationService.getViewsForEvent(event.getId());
                    return EventMapper.toFullDto(event, confirmedRequests, views);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Ивент с ID " + eventId + " не найден"));

        LocalDateTime now = LocalDateTime.now();
        if (request.getEventDate() != null && request.getEventDate().isBefore(now.plusHours(2))) {
            throw new ConflictException("Дата события должна быть не ранее чем через 2 часа от текущего момента.");
        }

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case "PUBLISH_EVENT":
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException("Невозможно опубликовать событие, так как оно не находится в состоянии PENDING.");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case "REJECT_EVENT":
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new ConflictException("Невозможно отклонить опубликованное событие");
                    }
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    break;
            }
        }

        Category category = null;
        if (request.getCategory() != null) {
            category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
        }

        EventMapper.applyAdminUpdate(event, request, category);

        event = eventRepository.save(event);
        Long confirmedRequests = getConfirmedRequests(event.getId());
        Long views = statsIntegrationService.getViewsForEvent(event.getId());
        return EventMapper.toFullDto(event, confirmedRequests, views);
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text,
                                               List<Long> categories,
                                               Boolean paid,
                                               LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd,
                                               Boolean onlyAvailable,
                                               String sort,
                                               Integer from,
                                               Integer size) {

        LocalDateTime now = LocalDateTime.now();
        if (rangeStart == null) {
            rangeStart = now;
        }
        if (rangeEnd == null) {
            rangeEnd = now.plusYears(1);
        }
        if (rangeEnd.isBefore(rangeStart)) {
            throw new ConflictException("Дата окончания не может быть раньше даты начала.");
        }

        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.searchPublic(text, categories, paid, rangeStart, rangeEnd, pageable);

        List<EventShortDto> result = events.stream()
                .map(event -> {
                    Long confirmedRequests = getConfirmedRequests(event.getId());
                    Long views = statsIntegrationService.getViewsForEvent(event.getId());
                    return EventMapper.toShortDto(event, confirmedRequests, views);
                })
                .collect(Collectors.toList());

        if (Boolean.TRUE.equals(onlyAvailable)) {
            result = result.stream()
                    .filter(dto -> dto.getConfirmedRequests() < eventRepository.findById(dto.getId())
                            .map(Event::getParticipantLimit)
                            .orElse(0))
                    .collect(Collectors.toList());
        }

        if ("VIEWS".equalsIgnoreCase(sort)) {
            result.sort((a, b) -> Long.compare(b.getViews(), a.getViews()));
        } else {
            result.sort((a, b) -> a.getEventDate().compareTo(b.getEventDate()));
        }

        return result;
    }

    @Override
    public EventFullDto getPublicEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Ивент с ID " + eventId + " не найден"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Ивент с ID " + eventId + " не найден");
        }

        statsIntegrationService.sendHit("main-service", "/events/" + eventId, "127.0.0.1", LocalDateTime.now());

        Long confirmedRequests = getConfirmedRequests(eventId);
        Long views = statsIntegrationService.getViewsForEvent(eventId);
        return EventMapper.toFullDto(event, confirmedRequests, views);
    }

    private Long getConfirmedRequests(Long eventId) {
        return (long) requestRepository.findByEventIdAndStatus(eventId, RequestStatus.CONFIRMED).size();
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Ивент с ID " + eventId + " не найден"));
        eventRepository.delete(event);
        log.info("Удалено событие с Id: {}", eventId);
    }
}
