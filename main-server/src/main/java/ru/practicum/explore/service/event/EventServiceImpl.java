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
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " was not found"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id " + newEventDto.getCategory() + " was not found"));

        LocalDateTime now = LocalDateTime.now();
        if (newEventDto.getEventDate().isBefore(now.plusHours(2))) {
            throw new ConflictException("Event date must be at least 2 hours from now");
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
            throw new NotFoundException("User with id " + userId + " was not found");
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
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " was not found"));

        Long confirmedRequests = getConfirmedRequests(event.getId());
        Long views = statsIntegrationService.getViewsForEvent(event.getId());
        return EventMapper.toFullDto(event, confirmedRequests, views);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " was not found"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        LocalDateTime now = LocalDateTime.now();
        if (request.getEventDate() != null && request.getEventDate().isBefore(now.plusHours(2))) {
            throw new ConflictException("Event date must be at least 2 hours from now");
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
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " was not found"));

        if (request.getStateAction() != null) {
            if (request.getStateAction().equals("PUBLISH_EVENT")) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Cannot publish event because it's not in PENDING state");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (request.getStateAction().equals("REJECT_EVENT")) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Cannot reject published event");
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
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " was not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id " + eventId + " was not found");
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
                        .orElseThrow(() -> new NotFoundException("Category not found"));
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
                        .orElseThrow(() -> new NotFoundException("Category not found"));
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
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " was not found"));
        eventRepository.delete(event);
        log.info("Deleted event with id: {}", eventId);
    }
}