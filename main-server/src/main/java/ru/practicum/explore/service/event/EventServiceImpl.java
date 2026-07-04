package ru.practicum.explore.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.dto.event.*;
import ru.practicum.explore.exception.ConflictException;
import ru.practicum.explore.exception.NotFoundException;
import ru.practicum.explore.mapper.EventMapper;
import ru.practicum.explore.model.category.Category;
import ru.practicum.explore.model.event.Event;
import ru.practicum.explore.model.event.EventState;
import ru.practicum.explore.model.location.EventLocation;
import ru.practicum.explore.model.participationRequest.RequestStatus;
import ru.practicum.explore.model.user.User;
import ru.practicum.explore.repository.category.CategoryRepository;
import ru.practicum.explore.repository.event.EventRepository;
import ru.practicum.explore.repository.participation.ParticipationRequestRepository;
import ru.practicum.explore.repository.user.UserRepository;
import ru.practicum.explore.service.stats.StatsIntegrationService;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);

        Specification<Event> spec = (root, query, criteriaBuilder) -> {
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
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            }

            if (rangeEnd != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

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
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Ивент с ID " + eventId + " не найден"));

        if (request.getEventDate() != null && request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new IllegalArgumentException("Дата события должна быть не ранее чем через 2 часа от текущего момента.");
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Изменить можно только ожидающие или отмененные события.");
        }

        // Обработка stateAction
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

        // Валидация даты
        if (request.getEventDate() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (request.getEventDate().isBefore(now.plusHours(2))) {
                throw new ConflictException("Дата события должна быть не ранее чем через 2 часа от текущего момента.");
            }
        }

        // Обновляем остальные поля
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
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
                    .lat(request.getLocationDto().getLat())
                    .lon(request.getLocationDto().getLon())
                    .build());
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Updated event: {}", updatedEvent);

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
            rangeEnd = now.plusYears(100);
        }

        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.findPublishedEvents(text, categories, paid, rangeStart, rangeEnd, pageable);

        // Фильтруем по доступности
        if (Boolean.TRUE.equals(onlyAvailable)) {
            events = events.stream()
                    .filter(event -> {
                        Long confirmedRequests = getConfirmedRequests(event.getId());
                        int participantLimit = event.getParticipantLimit() != null ? event.getParticipantLimit() : 0;
                        return participantLimit == 0 || confirmedRequests < participantLimit;
                    })
                    .collect(Collectors.toList());
        }

        // Сортируем
        if (sort != null) {
            if (sort.equals("EVENT_DATE")) {
                events.sort(Comparator.comparing(Event::getEventDate));
            } else if (sort.equals("VIEWS")) {
                Map<Long, Long> viewsMap = events.stream()
                        .collect(Collectors.toMap(
                                Event::getId,
                                event -> statsIntegrationService.getViewsForEvent(event.getId())
                        ));
                events.sort((e1, e2) -> {
                    Long views1 = viewsMap.getOrDefault(e1.getId(), 0L);
                    Long views2 = viewsMap.getOrDefault(e2.getId(), 0L);
                    return views2.compareTo(views1);
                });
            }
        }

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

        // Отправляем hit ДО получения views
        statsIntegrationService.sendHit("main-service", "/events/" + eventId, "127.0.0.1", LocalDateTime.now());

        Long confirmedRequests = getConfirmedRequests(eventId);
        // Получаем обновленные views после отправки hit
        Long views = statsIntegrationService.getViewsForEvent(eventId);
        return EventMapper.toFullDto(event, confirmedRequests, views);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Ивент с ID " + eventId + " не найден"));

        // Обработка stateAction
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

        // ВАЖНО: Обновляем остальные поля ДО сохранения
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getEventDate() != null) {
            // Валидация даты для админа
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConflictException("Дата события должна быть не ранее чем через 1 час от текущего момента.");
            }
            event.setEventDate(request.getEventDate());
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
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
                    .lat(request.getLocationDto().getLat())
                    .lon(request.getLocationDto().getLon())
                    .build());
        }

        // СОХРАНЯЕМ событие
        Event updatedEvent = eventRepository.save(event);
        log.info("Admin updated event: {}", updatedEvent);

        Long confirmedRequests = getConfirmedRequests(event.getId());
        Long views = statsIntegrationService.getViewsForEvent(event.getId());
        return EventMapper.toFullDto(updatedEvent, confirmedRequests, views);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Ивент с ID " + eventId + " не найден"));
        eventRepository.delete(event);
        log.info("Удалено событие с Id: {}", eventId);
    }

    private Long getConfirmedRequests(Long eventId) {
        return (long) requestRepository.findByEventIdAndStatus(eventId, RequestStatus.CONFIRMED).size();
    }
}