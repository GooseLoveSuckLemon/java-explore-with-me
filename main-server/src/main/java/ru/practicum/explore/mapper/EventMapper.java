package ru.practicum.explore.mapper;

import ru.practicum.explore.dto.category.CategoryDto;
import ru.practicum.explore.dto.event.EventFullDto;
import ru.practicum.explore.dto.event.EventShortDto;
import ru.practicum.explore.dto.event.NewEventDto;
import ru.practicum.explore.dto.event.UpdateEventAdminRequest;
import ru.practicum.explore.dto.event.UpdateEventUserRequest;
import ru.practicum.explore.dto.location.LocationDto;
import ru.practicum.explore.dto.user.UserShortDto;
import ru.practicum.explore.model.category.Category;
import ru.practicum.explore.model.event.Event;
import ru.practicum.explore.model.event.EventState;
import ru.practicum.explore.model.location.EventLocation;
import ru.practicum.explore.model.user.User;

import java.time.LocalDateTime;

public class EventMapper {

    public static Event toEntity(NewEventDto dto, User initiator, Category category) {
        return Event.builder()
                .annotation(dto.getAnnotation())
                .category(category)
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .initiator(initiator)
                .eventLocation(dto.getLocation() != null
                        ? new EventLocation(dto.getLocation().getLat(), dto.getLocation().getLon())
                        : new EventLocation(0f, 0f))
                .paid(dto.getPaid() != null ? dto.getPaid() : false)
                .participantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0)
                .requestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true)
                .title(dto.getTitle())
                .state(EventState.PENDING)
                .createdOn(LocalDateTime.now())
                .build();
    }

    public static void applyAdminUpdate(Event event, UpdateEventAdminRequest dto, Category category) {

        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());
        if (dto.getEventDate() != null) event.setEventDate(dto.getEventDate());

        if (dto.getCategory() != null && category != null) {
            event.setCategory(category);
        }

        if (dto.getLocationDto() != null) {
            event.setEventLocation(new EventLocation(
                    dto.getLocationDto().getLat(),
                    dto.getLocationDto().getLon()
            ));
        }

        if ("PUBLISH_EVENT".equals(dto.getStateAction())) {
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        }

        if ("REJECT_EVENT".equals(dto.getStateAction())) {
            event.setState(EventState.CANCELED);
        }
    }

    public static void applyUserUpdate(Event event, UpdateEventUserRequest dto, Category category) {

        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());
        if (dto.getEventDate() != null) event.setEventDate(dto.getEventDate());

        if (dto.getCategory() != null && category != null) {
            event.setCategory(category);
        }

        if (dto.getLocationDto() != null) {
            event.setEventLocation(new EventLocation(
                    dto.getLocationDto().getLat(),
                    dto.getLocationDto().getLon()
            ));
        }

        // Используем "CANCEL" для отмены события
        if ("CANCEL".equals(dto.getStateAction())) {
            event.setState(EventState.CANCELED);
        }
    }

    public static EventFullDto toFullDto(Event event, Long confirmedRequests, Long views) {
        if (event == null) {
            return null;
        }

        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(event.getCategory() != null ? CategoryMapper.toDto(event.getCategory()) : null)
                .confirmedRequests(confirmedRequests != null ? confirmedRequests : 0L)
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(event.getInitiator() != null ? UserMapper.toShortDto(event.getInitiator()) : null)
                .location(event.getEventLocation() != null ? LocationMapper.toLocationDto(event.getEventLocation()) : null)
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState() != null ? event.getState().name() : null)
                .title(event.getTitle())
                .views(views != null ? views : 0L)
                .build();
    }

    public static EventShortDto toShortDto(Event event, Long confirmedRequests, Long views) {

        CategoryDto categoryDto = event.getCategory() != null
                ? new CategoryDto(event.getCategory().getId(), event.getCategory().getName())
                : new CategoryDto(0L, "Unknown");

        UserShortDto initiatorDto = event.getInitiator() != null
                ? new UserShortDto(event.getInitiator().getId(), event.getInitiator().getName())
                : new UserShortDto(0L, "Unknown");

        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation() != null ? event.getAnnotation() : "")
                .category(categoryDto)
                .confirmedRequests(confirmedRequests != null ? confirmedRequests : 0L)
                .eventDate(event.getEventDate() != null ? event.getEventDate() : LocalDateTime.now())
                .initiator(initiatorDto)
                .paid(event.getPaid() != null ? event.getPaid() : false)
                .title(event.getTitle() != null ? event.getTitle() : "")
                .views(views != null ? views : 0L)
                .build();
    }
}