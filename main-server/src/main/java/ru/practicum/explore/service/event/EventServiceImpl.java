package ru.practicum.explore.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.dto.event.EventFullDto;
import ru.practicum.explore.dto.event.EventShortDto;
import ru.practicum.explore.dto.event.NewEventDto;
import ru.practicum.explore.dto.event.UpdateEventAdminRequest;
import ru.practicum.explore.dto.event.UpdateEventUserRequest;
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
        log.info("Created event: {}", event);
        return EventMapper.toFullDto(event, 0L, 0L);
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
        Event event = eventRepository.findByIdAndInitiatorId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("Ивент с ID " + eventId + " не найден"));

        Long confirmedRequests = getConfirmedRequests(event.getId());
        Long views = statsIntegrationService.getViewsForEvent(event.getId());
        return EventMapper.toFullDto(event, confirmedRequests, views);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = eventRepository.findByIdAndInitiatorId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("Ивент с ID " + eventId + " не найден"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Изменить можно только ожидающие или отмененные события.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (request.getEventDate() != null && request.getEventDate().isBefore(now.plusHours(2))) {
            throw new ConflictException("Дата события должна быть не ранее чем через 2 часа от текущего момента.");
        }

        Event updatedEvent = updateEventFields(event, request);
        updatedEvent = eventRepository.save(updatedEvent);
        log.info("Updated event: {}", updatedEvent);

        Long confirmedRequests = getConfirmedRequests(event.getId());
        Long views = statsIntegrationService.getViewsForEvent(event.getId());
        return EventMapper.toFullDto(updatedEvent, confirmedRequests, views);
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return eventRepository.findAll(pageable).getContent().stream()
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

        if (request.getStateAction() != null) {
            if (request.getStateAction().equals("PUBLISH_EVENT")) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Невозможно опубликовать событие, так как оно не находится в состоянии PENDING.");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (request.getStateAction().equals("REJECT_EVENT")) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Невозможно отклонить опубликованное событие");
                }
                event.setState(EventState.CANCELED);
            }
        }

        Event updatedEvent = updateEventFields(event, request);
        updatedEvent = eventRepository.save(updatedEvent);
        log.info("Admin updated event: {}", updatedEvent);

        Long confirmedRequests = getConfirmedRequests(event.getId());
        Long views = statsIntegrationService.getViewsForEvent(event.getId());
        return EventMapper.toFullDto(updatedEvent, confirmedRequests, views);
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, String sort, Integer from, Integer size) {

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
        }

        LocalDateTime now = LocalDateTime.now();

        if (rangeStart == null) {
            rangeStart = now;
        }
        if (rangeEnd == null) {
            rangeEnd = now.plusYears(1);
        }

        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.findPublishedEvents(text, categories, paid, rangeStart, rangeEnd, pageable);
        
        return events.stream()
                .map(event -> {
                    Long confirmedRequests = getConfirmedRequests(event.getId());
                    Long views = statsIntegrationService.getViewsForEvent(event.getId());
                    return EventMapper.toShortDto(event, confirmedRequests, views);
                })
                .collect(Collectors.toList());
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

    private Event updateEventFields(Event event, Object request) {
        if (request instanceof UpdateEventUserRequest) {
            UpdateEventUserRequest req = (UpdateEventUserRequest) request;
            if (req.getAnnotation() != null) event.setAnnotation(req.getAnnotation());
            if (req.getDescription() != null) event.setDescription(req.getDescription());
            if (req.getTitle() != null) event.setTitle(req.getTitle());
            if (req.getEventDate() != null) event.setEventDate(req.getEventDate());
            if (req.getPaid() != null) event.setPaid(req.getPaid());
            if (req.getParticipantLimit() != null) event.setParticipantLimit(req.getParticipantLimit());
            if (req.getRequestModeration() != null) event.setRequestModeration(req.getRequestModeration());
            if (req.getCategory() != null) {
                Category category = categoryRepository.findById(req.getCategory())
                        .orElseThrow(() -> new NotFoundException("Категория не найдена"));
                event.setCategory(category);
            }
            if (req.getLocationDto() != null) {
                event.setEventLocation(ru.practicum.explore.model.location.EventLocation.builder()
                        .lat(req.getLocationDto().getLat())
                        .lon(req.getLocationDto().getLon())
                        .build());
            }
        } else if (request instanceof UpdateEventAdminRequest) {
            UpdateEventAdminRequest req = (UpdateEventAdminRequest) request;
            if (req.getAnnotation() != null) event.setAnnotation(req.getAnnotation());
            if (req.getDescription() != null) event.setDescription(req.getDescription());
            if (req.getTitle() != null) event.setTitle(req.getTitle());
            if (req.getEventDate() != null) event.setEventDate(req.getEventDate());
            if (req.getPaid() != null) event.setPaid(req.getPaid());
            if (req.getParticipantLimit() != null) event.setParticipantLimit(req.getParticipantLimit());
            if (req.getRequestModeration() != null) event.setRequestModeration(req.getRequestModeration());
            if (req.getCategory() != null) {
                Category category = categoryRepository.findById(req.getCategory())
                        .orElseThrow(() -> new NotFoundException("Категория не найдена"));
                event.setCategory(category);
            }
            if (req.getLocationDto() != null) {
                event.setEventLocation(ru.practicum.explore.model.location.EventLocation.builder()
                        .lat(req.getLocationDto().getLat())
                        .lon(req.getLocationDto().getLon())
                        .build());
            }
        }
        return event;
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