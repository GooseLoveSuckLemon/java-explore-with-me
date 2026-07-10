package ru.practicum.server.mapper.event;

import ru.practicum.server.dto.category.CategoryDto;
import ru.practicum.server.dto.event.EventDto;
import ru.practicum.server.dto.event.EventShortDto;
import ru.practicum.server.dto.event.NewEventDto;
import ru.practicum.server.dto.event.update.UpdateEventAdminRequest;
import ru.practicum.server.dto.event.update.UpdateEventUserRequest;
import ru.practicum.server.dto.user.UserShortDto;
import ru.practicum.server.mapper.category.CategoryMapper;
import ru.practicum.server.mapper.location.LocationMapper;
import ru.practicum.server.mapper.user.UserMapper;
import ru.practicum.server.model.category.Category;
import ru.practicum.server.model.event.Event;
import ru.practicum.server.model.event.EventState;
import ru.practicum.server.model.location.EventLocation;
import ru.practicum.server.model.user.User;

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

    public static void applyAdminUpdate(Event event, UpdateEventAdminRequest update, Category category) {

        if (update.getAnnotation() != null) event.setAnnotation(update.getAnnotation());
        if (update.getDescription() != null) event.setDescription(update.getDescription());
        if (update.getTitle() != null) event.setTitle(update.getTitle());
        if (update.getPaid() != null) event.setPaid(update.getPaid());
        if (update.getParticipantLimit() != null) event.setParticipantLimit(update.getParticipantLimit());
        if (update.getRequestModeration() != null) event.setRequestModeration(update.getRequestModeration());
        if (update.getEventDate() != null) event.setEventDate(update.getEventDate());

        if (update.getCategory() != null && category != null) {
            event.setCategory(category);
        }

        if (update.getLocationDto() != null) {
            event.setEventLocation(new EventLocation(
                    update.getLocationDto().getLat(),
                    update.getLocationDto().getLon()
            ));
        }

        if ("PUBLISH_EVENT".equals(update.getStateAction())) {
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        }

        if ("REJECT_EVENT".equals(update.getStateAction())) {
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

        if ("CANCEL".equals(dto.getStateAction())) {
            event.setState(EventState.CANCELED);
        }
    }

    public static EventDto toFullDto(Event event, Long confirmedRequests, Long views) {
        if (event == null) {
            return null;
        }

        return EventDto.builder()
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