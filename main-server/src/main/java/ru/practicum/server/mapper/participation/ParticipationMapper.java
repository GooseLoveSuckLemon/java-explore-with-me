package ru.practicum.server.mapper.participation;

import ru.practicum.server.dto.participation.ParticipationRequestDto;
import ru.practicum.server.model.participation.ParticipationRequest;

public class ParticipationMapper {

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
