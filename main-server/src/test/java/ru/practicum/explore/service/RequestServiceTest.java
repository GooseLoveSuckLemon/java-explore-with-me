package ru.practicum.explore.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.explore.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.explore.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.explore.dto.participation.ParticipationRequestDto;
import ru.practicum.explore.exception.ConflictException;
import ru.practicum.explore.exception.NotFoundException;
import ru.practicum.explore.model.category.Category;
import ru.practicum.explore.model.event.Event;
import ru.practicum.explore.model.event.EventState;
import ru.practicum.explore.model.participationRequest.ParticipationRequest;
import ru.practicum.explore.model.participationRequest.RequestStatus;
import ru.practicum.explore.model.user.User;
import ru.practicum.explore.repository.event.EventRepository;
import ru.practicum.explore.repository.participation.ParticipationRequestRepository;
import ru.practicum.explore.repository.user.UserRepository;
import ru.practicum.explore.service.request.RequestServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private ParticipationRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private RequestServiceImpl requestService;

    @Test
    void addParticipationRequest_ShouldReturnRequestDto() {
        Long userId = 2L;
        Long eventId = 1L;

        User user = User.builder()
                .id(userId)
                .email("requester@test.com")
                .name("Requester")
                .build();

        User initiator = User.builder()
                .id(1L)
                .email("initiator@test.com")
                .name("Initiator")
                .build();

        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        Event event = Event.builder()
                .id(eventId)
                .annotation("Тестовое событие")
                .category(category)
                .initiator(initiator)
                .state(EventState.PUBLISHED)
                .participantLimit(10)
                .requestModeration(true)
                .title("Тестовое событие")
                .description("Описание")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.existsByRequesterIdAndEventId(userId, eventId)).thenReturn(false);
        when(requestRepository.findByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)).thenReturn(List.of());
        when(requestRepository.save(any(ParticipationRequest.class))).thenAnswer(invocation -> {
            ParticipationRequest request = invocation.getArgument(0);
            request.setId(1L);
            return request;
        });

        ParticipationRequestDto result = requestService.addParticipationRequest(userId, eventId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(eventId, result.getEvent());
        assertEquals(userId, result.getRequester());
        assertEquals(RequestStatus.PENDING.name(), result.getStatus());

        verify(userRepository).findById(userId);
        verify(eventRepository).findById(eventId);
        verify(requestRepository).save(any(ParticipationRequest.class));
    }

    @Test
    void addParticipationRequest_WithUserNotFound_ShouldThrowNotFoundException() {
        Long userId = 2L;
        Long eventId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.addParticipationRequest(userId, eventId));

        verify(userRepository).findById(userId);
        verify(eventRepository, never()).findById(anyLong());
        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }

    @Test
    void addParticipationRequest_WithEventNotFound_ShouldThrowNotFoundException() {
        Long userId = 2L;
        Long eventId = 1L;

        User user = User.builder().id(userId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.addParticipationRequest(userId, eventId));

        verify(userRepository).findById(userId);
        verify(eventRepository).findById(eventId);
        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }

    @Test
    void addParticipationRequest_ToOwnEvent_ShouldThrowConflictException() {
        Long userId = 1L;
        Long eventId = 1L;

        User user = User.builder().id(userId).build();
        Event event = Event.builder()
                .id(eventId)
                .initiator(user)
                .state(EventState.PUBLISHED)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThrows(ConflictException.class, () -> requestService.addParticipationRequest(userId, eventId));

        verify(userRepository).findById(userId);
        verify(eventRepository).findById(eventId);
        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }

    @Test
    void addParticipationRequest_ToUnpublishedEvent_ShouldThrowConflictException() {
        Long userId = 2L;
        Long eventId = 1L;

        User user = User.builder().id(userId).build();
        User initiator = User.builder().id(1L).build();
        Event event = Event.builder()
                .id(eventId)
                .initiator(initiator)
                .state(EventState.PENDING)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThrows(ConflictException.class, () -> requestService.addParticipationRequest(userId, eventId));

        verify(userRepository).findById(userId);
        verify(eventRepository).findById(eventId);
        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }

    @Test
    void addParticipationRequest_AlreadyExists_ShouldThrowConflictException() {
        Long userId = 2L;
        Long eventId = 1L;

        User user = User.builder().id(userId).build();
        User initiator = User.builder().id(1L).build();
        Event event = Event.builder()
                .id(eventId)
                .initiator(initiator)
                .state(EventState.PUBLISHED)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.existsByRequesterIdAndEventId(userId, eventId)).thenReturn(true);

        assertThrows(ConflictException.class, () -> requestService.addParticipationRequest(userId, eventId));

        verify(userRepository).findById(userId);
        verify(eventRepository).findById(eventId);
        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }

    @Test
    void addParticipationRequest_WithParticipantLimitReached_ShouldThrowConflictException() {
        Long userId = 2L;
        Long eventId = 1L;

        User user = User.builder().id(userId).build();
        User initiator = User.builder().id(1L).build();
        Event event = Event.builder()
                .id(eventId)
                .initiator(initiator)
                .state(EventState.PUBLISHED)
                .participantLimit(1)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.existsByRequesterIdAndEventId(userId, eventId)).thenReturn(false);
        when(requestRepository.findByEventIdAndStatus(eventId, RequestStatus.CONFIRMED))
                .thenReturn(List.of(new ParticipationRequest()));

        assertThrows(ConflictException.class, () -> requestService.addParticipationRequest(userId, eventId));

        verify(userRepository).findById(userId);
        verify(eventRepository).findById(eventId);
        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }

    @Test
    void cancelRequest_ShouldReturnCanceledRequest() {
        Long userId = 1L;
        Long requestId = 1L;

        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .name("Test User")
                .build();

        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        Event event = Event.builder()
                .id(1L)
                .annotation("Событие")
                .category(category)
                .title("Тестовое событие")
                .description("Описание")
                .build();

        ParticipationRequest requestEntity = ParticipationRequest.builder()
                .id(requestId)
                .requester(user)
                .event(event)
                .status(RequestStatus.PENDING)
                .build();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(requestEntity));
        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(requestEntity);

        ParticipationRequestDto result = requestService.cancelRequest(userId, requestId);

        assertNotNull(result);
        assertEquals(RequestStatus.CANCELED.name(), result.getStatus());

        verify(requestRepository).findById(requestId);
        verify(requestRepository).save(any(ParticipationRequest.class));
    }

    @Test
    void cancelRequest_WithInvalidUser_ShouldThrowNotFoundException() {
        Long userId = 2L;
        Long requestId = 1L;

        User user = User.builder().id(1L).build();
        ParticipationRequest requestEntity = ParticipationRequest.builder()
                .id(requestId)
                .requester(user)
                .build();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(requestEntity));

        assertThrows(NotFoundException.class, () -> requestService.cancelRequest(userId, requestId));

        verify(requestRepository).findById(requestId);
        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }

    @Test
    void changeRequestStatus_ConfirmRequest_ShouldReturnConfirmed() {
        Long userId = 1L;
        Long eventId = 1L;

        EventRequestStatusUpdateRequest request = new EventRequestStatusUpdateRequest();
        request.setRequestIds(List.of(1L));
        request.setStatus("CONFIRMED");

        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        User initiator = User.builder()
                .id(userId)
                .email("test@test.com")
                .name("Test User")
                .build();

        Event event = Event.builder()
                .id(eventId)
                .annotation("Событие")
                .category(category)
                .initiator(initiator)
                .participantLimit(10)
                .title("Тестовое событие")
                .description("Описание")
                .build();

        User requester = User.builder()
                .id(2L)
                .email("requester@test.com")
                .name("Requester")
                .build();

        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .id(1L)
                .event(event)
                .requester(requester)
                .status(RequestStatus.PENDING)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.findByIdIn(anyList())).thenReturn(List.of(participationRequest));
        when(requestRepository.findByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)).thenReturn(List.of());
        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(participationRequest);

        EventRequestStatusUpdateResult result = requestService.changeRequestStatus(userId, eventId, request);

        assertNotNull(result);
        assertEquals(1, result.getConfirmedRequests().size());
        assertEquals(0, result.getRejectedRequests().size());

        verify(eventRepository).findById(eventId);
        verify(requestRepository).findByIdIn(anyList());
        verify(requestRepository, atLeastOnce()).save(any(ParticipationRequest.class));
    }

    @Test
    void changeRequestStatus_RejectRequest_ShouldReturnRejected() {
        Long userId = 1L;
        Long eventId = 1L;

        EventRequestStatusUpdateRequest request = new EventRequestStatusUpdateRequest();
        request.setRequestIds(List.of(1L));
        request.setStatus("REJECTED");

        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        User initiator = User.builder()
                .id(userId)
                .email("test@test.com")
                .name("Test User")
                .build();

        Event event = Event.builder()
                .id(eventId)
                .annotation("Событие")
                .category(category)
                .initiator(initiator)
                .participantLimit(10)
                .title("Тестовое событие")
                .description("Описание")
                .build();

        User requester = User.builder()
                .id(2L)
                .email("requester@test.com")
                .name("Requester")
                .build();

        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .id(1L)
                .event(event)
                .requester(requester)
                .status(RequestStatus.PENDING)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.findByIdIn(anyList())).thenReturn(List.of(participationRequest));
        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(participationRequest);

        EventRequestStatusUpdateResult result = requestService.changeRequestStatus(userId, eventId, request);

        assertNotNull(result);
        assertEquals(0, result.getConfirmedRequests().size());
        assertEquals(1, result.getRejectedRequests().size());

        verify(eventRepository).findById(eventId);
        verify(requestRepository).findByIdIn(anyList());
        verify(requestRepository, atLeastOnce()).save(any(ParticipationRequest.class));
    }

    @Test
    void changeRequestStatus_WithInvalidStatus_ShouldThrowConflictException() {
        Long userId = 1L;
        Long eventId = 1L;

        EventRequestStatusUpdateRequest request = new EventRequestStatusUpdateRequest();
        request.setRequestIds(List.of(1L));
        request.setStatus("CONFIRMED");

        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        User initiator = User.builder()
                .id(userId)
                .email("test@test.com")
                .name("Test User")
                .build();

        Event event = Event.builder()
                .id(eventId)
                .annotation("Событие")
                .category(category)
                .initiator(initiator)
                .title("Тестовое событие")
                .description("Описание")
                .build();

        User requester = User.builder()
                .id(2L)
                .email("requester@test.com")
                .name("Requester")
                .build();

        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .id(1L)
                .event(event)
                .requester(requester)
                .status(RequestStatus.CONFIRMED)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.findByIdIn(anyList())).thenReturn(List.of(participationRequest));

        assertThrows(ConflictException.class, () -> requestService.changeRequestStatus(userId, eventId, request));

        verify(eventRepository).findById(eventId);
        verify(requestRepository).findByIdIn(anyList());
        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }
}