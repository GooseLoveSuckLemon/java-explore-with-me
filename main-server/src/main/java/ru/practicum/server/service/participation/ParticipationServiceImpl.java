package ru.practicum.server.service.participation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.server.dto.event.update.EventRequestStatusUpdate;
import ru.practicum.server.dto.event.update.EventRequestStatusUpdateResult;
import ru.practicum.server.dto.participation.ParticipationRequestDto;
import ru.practicum.server.exception.ConflictException;
import ru.practicum.server.exception.NotFoundException;
import ru.practicum.server.mapper.participation.ParticipationMapper;
import ru.practicum.server.model.event.Event;
import ru.practicum.server.model.event.EventState;
import ru.practicum.server.model.participation.ParticipationRequest;
import ru.practicum.server.model.participation.ParticipationStatus;
import ru.practicum.server.model.user.User;
import ru.practicum.server.repository.event.EventRepository;
import ru.practicum.server.repository.participation.ParticipationRequestRepository;
import ru.practicum.server.repository.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipationServiceImpl implements ParticipationService {
    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        return requestRepository.findByRequesterId(userId).stream()
                .map(ParticipationMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор не может участвовать в собственном событии.");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Невозможно принять участие в неопубликованном мероприятии");
        }

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Запрос уже существует");
        }

        List<ParticipationRequest> confirmedRequests = requestRepository.findByEventIdAndStatus(eventId, ParticipationStatus.CONFIRMED);
        int participantLimit = event.getParticipantLimit() != null ? event.getParticipantLimit() : 0;
        if (participantLimit > 0 && confirmedRequests.size() >= participantLimit) {
            throw new ConflictException("Лимит участников достигнут");
        }

        ParticipationStatus initialStatus = (participantLimit == 0 || !event.getRequestModeration())
                ? ParticipationStatus.CONFIRMED
                : ParticipationStatus.PENDING;

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(initialStatus)
                .build();

        request = requestRepository.save(request);
        log.info("Created request: {}", request);
        return ParticipationMapper.toDto(request);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с идентификатором " + requestId + " не найден"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Запрос с идентификатором " + requestId + " не найден");
        }

        request.setStatus(ParticipationStatus.CANCELED);
        request = requestRepository.save(request);
        log.info("Отмененный запрос: {}", request);
        return ParticipationMapper.toDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие с ID " + eventId + " не найдено");
        }

        return requestRepository.findByEventId(eventId).stream()
                .map(ParticipationMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdate request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие с ID " + eventId + " не найдено");
        }

        List<ParticipationRequest> requests = requestRepository.findByIdIn(request.getRequestIds());

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (ParticipationRequest req : requests) {
            if (req.getStatus() != ParticipationStatus.PENDING) {
                throw new ConflictException("Запрос должен иметь статус PENDING.");
            }

            if (request.getStatus().equals("CONFIRMED")) {
                int participantLimit = event.getParticipantLimit() != null ? event.getParticipantLimit() : 0;
                if (participantLimit > 0) {
                    List<ParticipationRequest> confirmedRequests = requestRepository.findByEventIdAndStatus(eventId, ParticipationStatus.CONFIRMED);
                    if (confirmedRequests.size() >= participantLimit) {
                        throw new ConflictException("Достигнут лимит участников");
                    }
                }
                req.setStatus(ParticipationStatus.CONFIRMED);
                confirmed.add(ParticipationMapper.toDto(req));

                if (event.getParticipantLimit() != null && event.getParticipantLimit() > 0) {
                    List<ParticipationRequest> pendingRequests = requestRepository.findByEventIdAndStatus(
                            eventId, ParticipationStatus.PENDING);
                    if (requestRepository.findByEventIdAndStatus(eventId, ParticipationStatus.CONFIRMED).size() >= event.getParticipantLimit()) {
                        for (ParticipationRequest pending : pendingRequests) {
                            if (!request.getRequestIds().contains(pending.getId())) {
                                pending.setStatus(ParticipationStatus.REJECTED);
                                requestRepository.save(pending);
                                rejected.add(ParticipationMapper.toDto(pending));
                            }
                        }
                    }
                }
            } else if (request.getStatus().equals("REJECTED")) {
                req.setStatus(ParticipationStatus.REJECTED);
                rejected.add(ParticipationMapper.toDto(req));
            }

            requestRepository.save(req);
        }

        log.info("Changed request statuses: confirmed={}, rejected={}", confirmed.size(), rejected.size());
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected)
                .build();
    }
}