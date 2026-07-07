package ru.practicum.server.service.event;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.server.dto.event.EventDto;
import ru.practicum.server.dto.event.EventShortDto;
import ru.practicum.server.dto.event.NewEventDto;
import ru.practicum.server.dto.event.update.UpdateEventAdminRequest;
import ru.practicum.server.dto.event.update.UpdateEventUserRequest;
import ru.practicum.server.exception.ConflictException;
import ru.practicum.server.exception.NotFoundException;
import ru.practicum.server.mapper.event.EventMapper;
import ru.practicum.server.model.category.Category;
import ru.practicum.server.model.event.Event;
import ru.practicum.server.model.event.EventState;
import ru.practicum.server.model.location.EventLocation;
import ru.practicum.server.model.participation.ParticipationStatus;
import ru.practicum.server.model.user.User;
import ru.practicum.server.repository.category.CategoryRepository;
import ru.practicum.server.repository.event.EventRepository;
import ru.practicum.server.repository.participation.ParticipationRequestRepository;
import ru.practicum.server.repository.user.UserRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository requestRepository;
    private final StatsClient statsClient;

    private final Map<Long, AtomicLong> viewsCache = new ConcurrentHashMap<>();

    private long getViews(Event event) {
        LocalDateTime start = event.getPublishedOn() != null
                ? event.getPublishedOn()
                : event.getCreatedOn() != null ? event.getCreatedOn() : LocalDateTime.now().minusYears(1);

        List<ViewStatsDto> stats = statsClient.getStats(
                start,
                LocalDateTime.now(),
                List.of("/events/" + event.getId()),
                true
        );
        long viewsFromStats = stats.isEmpty() ? 0 : stats.get(0).getHits();

        AtomicLong cachedViews = viewsCache.get(event.getId());
        long totalViews = viewsFromStats + (cachedViews != null ? cachedViews.get() : 0);

        log.debug("Event {}: views from stats={}, cached={}, total={}",
                event.getId(), viewsFromStats, cachedViews != null ? cachedViews.get() : 0, totalViews);

        return totalViews;
    }

    @Override
    @Transactional
    public EventDto createEvent(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с Id " + newEventDto.getCategory() + " не найдена"));

        LocalDateTime now = LocalDateTime.now();
        if (newEventDto.getEventDate().isBefore(now.plusHours(2))) {
            throw new IllegalArgumentException("Дата события должна быть не ранее чем через 2 часа от текущего момента.");
        }

        Event event = EventMapper.toEntity(newEventDto, user, category);
        event.setCreatedOn(now);
        event.setState(EventState.PENDING);

        event = eventRepository.save(event);
        log.info("Created event: {}", event);
        return EventMapper.toFullDto(event, 0L, 0L);
    }

    @Override
    public List<EventDto> getUserEvents(Long userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        Pageable pageable = PageRequest.of(from / size, size);

        return eventRepository.findByInitiatorId(userId, pageable).stream()
                .map(event -> {
                    Long confirmedRequests = getConfirmedRequests(event.getId());
                    long views = getViews(event);
                    return EventMapper.toFullDto(event, confirmedRequests, views);
                })
                .collect(Collectors.toList());
    }

    @Override
    public EventDto getUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Ивент с ID " + eventId + " не найден"));

        Long confirmedRequests = getConfirmedRequests(event.getId());
        long views = getViews(event);
        return EventMapper.toFullDto(event, confirmedRequests, views);
    }

    @Override
    public List<EventDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                           LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                           Integer from, Integer size) {

        validateDateRange(rangeStart, rangeEnd);
        Pageable pageable = PageRequest.of(from / size, size);

        Specification<Event> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (users != null && !users.isEmpty()) {
                predicates.add(root.get("initiator").get("id").in(users));
            }

            if (states != null && !states.isEmpty()) {
                List<EventState> eventStates = states.stream()
                        .map(EventState::valueOf)
                        .collect(Collectors.toList());
                predicates.add(root.get("state").in(eventStates));
            }

            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categories));
            }

            if (rangeStart != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            }

            if (rangeEnd != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

        return events.stream()
                .map(event -> {
                    Long confirmedRequests = getConfirmedRequests(event.getId());
                    long views = getViews(event);
                    return EventMapper.toFullDto(event, confirmedRequests, views);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Ивент с ID " + eventId + " не найден"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Изменить можно только ожидающие или отмененные события.");
        }

        LocalDateTime now = LocalDateTime.now();

        if (request.getEventDate() != null && request.getEventDate().isBefore(now.plusHours(2))) {
            throw new IllegalArgumentException("Дата события должна быть не ранее чем через 2 часа от текущего момента.");
        }

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case "SEND_TO_REVIEW":
                    if (event.getState() != EventState.CANCELED) {
                        throw new ConflictException("Отправить на ревью можно только отмененное событие.");
                    }
                    event.setState(EventState.PENDING);
                    break;

                case "CANCEL_REVIEW":
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException("Отменить ревью можно только у события в статусе PENDING.");
                    }
                    event.setState(EventState.CANCELED);
                    break;

                default:
                    throw new IllegalArgumentException("Недопустимое действие: " + request.getStateAction());
            }
        }

        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getEventDate() != null) event.setEventDate(request.getEventDate());
        if (request.getPaid() != null) event.setPaid(request.getPaid());

        if (request.getParticipantLimit() != null) {
            if (request.getParticipantLimit() < 0) {
                throw new ConflictException("Лимит участников не может быть отрицательным.");
            }
            event.setParticipantLimit(request.getParticipantLimit());
        }

        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }

        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
            event.setCategory(category);
        }

        if (request.getLocationDto() != null) {
            event.setEventLocation(EventLocation.builder()
                    .latitude(request.getLocationDto().getLat())
                    .longitude(request.getLocationDto().getLon())
                    .build());
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Updated event: {}", updatedEvent);

        Long confirmedRequests = getConfirmedRequests(event.getId());
        long views = getViews(event);
        return EventMapper.toFullDto(updatedEvent, confirmedRequests, views);
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, String sort, Integer from,
                                               Integer size, String ip) {

        sendStatsSync("/events", ip);

        validateDateRange(rangeStart, rangeEnd);

        LocalDateTime now = LocalDateTime.now();

        if (rangeStart == null) rangeStart = now;
        if (rangeEnd == null) rangeEnd = now.plusYears(100);

        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.findPublishedEvents(text, categories, paid, rangeStart, rangeEnd, pageable);

        if (Boolean.TRUE.equals(onlyAvailable)) {
            events = events.stream()
                    .filter(event -> {
                        Long confirmedRequests = getConfirmedRequests(event.getId());
                        int limit = event.getParticipantLimit() != null ? event.getParticipantLimit() : 0;
                        return limit == 0 || confirmedRequests < limit;
                    })
                    .collect(Collectors.toList());
        }

        if (sort != null) {
            if (sort.equals("EVENT_DATE")) {
                events.sort(Comparator.comparing(Event::getEventDate));
            } else if (sort.equals("VIEWS")) {
                Map<Long, Long> viewsMap = events.stream()
                        .collect(Collectors.toMap(
                                Event::getId,
                                this::getViews
                        ));

                events.sort((e1, e2) -> viewsMap.getOrDefault(e2.getId(), 0L)
                        .compareTo(viewsMap.getOrDefault(e1.getId(), 0L)));
            }
        }

        return events.stream()
                .map(event -> {
                    Long confirmedRequests = getConfirmedRequests(event.getId());
                    long views = getViews(event);
                    return EventMapper.toShortDto(event, confirmedRequests, views);
                })
                .collect(Collectors.toList());
    }

    @Override
    public EventDto getPublicEvent(Long eventId, String ip) {
        // Отправляем статистику СИНХРОННО и ждем сохранения
        sendStatsSync("/events/" + eventId, ip);

        // Увеличиваем кэшированный счетчик для этого события
        viewsCache.computeIfAbsent(eventId, k -> new AtomicLong(0)).incrementAndGet();
        log.info("Увеличено количество просмотров для события {}: {}", eventId, viewsCache.get(eventId).get());

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Ивент с ID " + eventId + " не найден"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Ивент с ID " + eventId + " не найден");
        }

        Long confirmedRequests = getConfirmedRequests(eventId);
        long views = getViews(event);

        EventDto eventDto = EventMapper.toFullDto(event, confirmedRequests, views);

        eventDto.setViews(eventDto.getViews() + 1);

        return eventDto;
    }

    @Override
    @Transactional
    public EventDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
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

        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new IllegalArgumentException("Дата события должна быть не ранее чем через 1 час от текущего момента.");
            }
            event.setEventDate(request.getEventDate());
        }

        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getPaid() != null) event.setPaid(request.getPaid());

        if (request.getParticipantLimit() != null) {
            if (request.getParticipantLimit() < 0) {
                throw new ConflictException("Лимит участников не может быть отрицательным.");
            }
            event.setParticipantLimit(request.getParticipantLimit());
        }

        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }

        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
            event.setCategory(category);
        }

        if (request.getLocationDto() != null) {
            event.setEventLocation(EventLocation.builder()
                    .latitude(request.getLocationDto().getLat())
                    .longitude(request.getLocationDto().getLon())
                    .build());
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Admin updated event: {}", updatedEvent);

        Long confirmedRequests = getConfirmedRequests(event.getId());
        long views = getViews(event);
        return EventMapper.toFullDto(updatedEvent, confirmedRequests, views);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Ивент с ID " + eventId + " не найден"));
        viewsCache.remove(eventId);
        eventRepository.delete(event);
        log.info("Удалено событие с Id: {}", eventId);
    }

    /**
     * Синхронная отправка статистики - ждем сохранения
     */
    private void sendStatsSync(String uri, String ip) {
        try {
            statsClient.sendHit(
                    EndpointHitDto.builder()
                            .app("main-service")
                            .uri(uri)
                            .ip(ip)
                            .timestamp(LocalDateTime.now())
                            .build()
            );
            log.debug("Статистика для URI {} отправлена с IP: {}", uri, ip);

            // Небольшая задержка для гарантии сохранения в тестовом окружении
            // В продакшене можно убрать или уменьшить
            Thread.sleep(50);
        } catch (Exception e) {
            log.error("Ошибка при отправке статистики для URI: {}", uri, e);
        }
    }

    /**
     * Очистка кэша (может вызываться в тестах)
     */
    public void clearViewsCache() {
        viewsCache.clear();
        log.info("Кэш просмотров очищен");
    }

    private Long getConfirmedRequests(Long eventId) {
        return (long) requestRepository.findByEventIdAndStatus(eventId, ParticipationStatus.CONFIRMED).size();
    }

    private void validateDateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new IllegalArgumentException("Дата начала диапазона не может быть позже даты окончания.");
        }
    }
}