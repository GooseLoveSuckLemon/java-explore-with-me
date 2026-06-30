package ru.practicum.explore.service.request;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.explore.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.explore.dto.participation.ParticipationRequestDto;
import ru.practicum.explore.exception.ConflictException;
import ru.practicum.explore.exception.NotFoundException;
import ru.practicum.explore.mapper.RequestMapper;
import ru.practicum.explore.model.event.Event;
import ru.practicum.explore.model.event.EventState;
import ru.practicum.explore.model.participationRequest.ParticipationRequest;
import ru.practicum.explore.model.participationRequest.RequestStatus;
import ru.practicum.explore.model.user.User;
import ru.practicum.explore.repository.event.EventRepository;
import ru.practicum.explore.repository.participation.ParticipationRequestRepository;
import ru.practicum.explore.repository.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " was not found");
        }
        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " was not found"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot participate in own event");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot participate in unpublished event");
        }

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Request already exists");
        }

        List<ParticipationRequest> confirmedRequests = requestRepository.findByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        int participantLimit = event.getParticipantLimit() != null ? event.getParticipantLimit() : 0;
        if (participantLimit > 0 && confirmedRequests.size() >= participantLimit) {
            throw new ConflictException("Participant limit has been reached");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(Boolean.TRUE.equals(event.getRequestModeration()) ? RequestStatus.PENDING : RequestStatus.CONFIRMED)
                .build();

        request = requestRepository.save(request);
        log.info("Created request: {}", request);
        return RequestMapper.toDto(request);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id " + requestId + " was not found"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request with id " + requestId + " was not found");
        }

        request.setStatus(RequestStatus.CANCELED);
        request = requestRepository.save(request);
        log.info("Canceled request: {}", request);
        return RequestMapper.toDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " was not found");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " was not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id " + eventId + " was not found");
        }

        return requestRepository.findByEventId(eventId).stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " was not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id " + eventId + " was not found");
        }

        List<ParticipationRequest> requests = requestRepository.findByIdIn(request.getRequestIds());

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (ParticipationRequest req : requests) {
            if (req.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }

            if (request.getStatus().equals("CONFIRMED")) {
                int participantLimit = event.getParticipantLimit() != null ? event.getParticipantLimit() : 0;
                if (participantLimit > 0) {
                    List<ParticipationRequest> confirmedRequests = requestRepository.findByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
                    if (confirmedRequests.size() >= participantLimit) {
                        throw new ConflictException("Participant limit has been reached");
                    }
                }
                req.setStatus(RequestStatus.CONFIRMED);
                confirmed.add(RequestMapper.toDto(req));

                // Если лимит исчерпан, отклоняем остальные
                if (event.getParticipantLimit() != null && event.getParticipantLimit() > 0) {
                    List<ParticipationRequest> pendingRequests = requestRepository.findByEventIdAndStatusIn(
                            eventId, List.of(RequestStatus.PENDING));
                    if (requestRepository.findByEventIdAndStatus(eventId, RequestStatus.CONFIRMED).size() >= event.getParticipantLimit()) {
                        for (ParticipationRequest pending : pendingRequests) {
                            if (!request.getRequestIds().contains(pending.getId())) {
                                pending.setStatus(RequestStatus.REJECTED);
                                requestRepository.save(pending);
                                rejected.add(RequestMapper.toDto(pending));
                            }
                        }
                    }
                }
            } else if (request.getStatus().equals("REJECTED")) {
                req.setStatus(RequestStatus.REJECTED);
                rejected.add(RequestMapper.toDto(req));
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