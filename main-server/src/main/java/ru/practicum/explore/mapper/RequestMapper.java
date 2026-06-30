package ru.practicum.explore.mapper;

import ru.practicum.explore.dto.participation.ParticipationRequestDto;
import ru.practicum.explore.model.participationRequest.ParticipationRequest;

public class RequestMapper {

    public static ParticipationRequestDto toDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus().name())
                .build();
    }
}