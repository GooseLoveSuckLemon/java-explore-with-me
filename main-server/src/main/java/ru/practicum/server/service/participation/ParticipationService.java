package ru.practicum.server.service.participation;

import ru.practicum.server.dto.event.update.EventRequestStatusUpdate;
import ru.practicum.server.dto.event.update.EventRequestStatusUpdateResult;
import ru.practicum.server.dto.participation.ParticipationRequestDto;

import java.util.List;

public interface ParticipationService {

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto addParticipationRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdate request);
}
