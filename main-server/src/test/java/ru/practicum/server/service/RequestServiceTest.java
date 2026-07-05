package ru.practicum.server.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.server.dto.event.update.EventRequestStatusUpdate;
import ru.practicum.server.dto.event.update.EventRequestStatusUpdateResult;
import ru.practicum.server.dto.participation.ParticipationRequestDto;
import ru.practicum.server.exception.ConflictException;
import ru.practicum.server.exception.NotFoundException;
import ru.practicum.server.model.category.Category;
import ru.practicum.server.model.event.Event;
import ru.practicum.server.model.event.EventState;
import ru.practicum.server.model.participation.ParticipationRequest;
import ru.practicum.server.model.participation.ParticipationStatus;
import ru.practicum.server.model.user.User;
import ru.practicum.server.repository.event.EventRepository;
import ru.practicum.server.repository.participation.ParticipationRequestRepository;
import ru.practicum.server.repository.user.UserRepository;
import ru.practicum.server.service.participation.ParticipationServiceImpl;

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
    private ParticipationServiceImpl participationService;

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
        when(requestRepository.findByEventIdAndStatus(eventId, ParticipationStatus.CONFIRMED)).thenReturn(List.of());
        when(requestRepository.save(any(ParticipationRequest.class))).thenAnswer(invocation -> {
            ParticipationRequest request = invocation.getArgument(0);
            request.setId(1L);
            return request;
        });

        ParticipationRequestDto result = participationService.addParticipationRequest(userId, eventId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(eventId, result.getEvent());
        assertEquals(userId, result.getRequester());
        assertEquals(ParticipationStatus.PENDING.name(), result.getStatus());

        verify(userRepository).findById(userId);
        verify(eventRepository).findById(eventId);
        verify(requestRepository).save(any(ParticipationRequest.class));
    }

    @Test
    void addParticipationRequest_WithUserNotFound_ShouldThrowNotFoundException() {
        Long userId = 2L;
        Long eventId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> participationService.addParticipationRequest(userId, eventId));

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

        assertThrows(NotFoundException.class, () -> participationService.addParticipationRequest(userId, eventId));

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

        assertThrows(ConflictException.class, () -> participationService.addParticipationRequest(userId, eventId));

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

        assertThrows(ConflictException.class, () -> participationService.addParticipationRequest(userId, eventId));

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

        assertThrows(ConflictException.class, () -> participationService.addParticipationRequest(userId, eventId));

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
        when(requestRepository.findByEventIdAndStatus(eventId, ParticipationStatus.CONFIRMED))
                .thenReturn(List.of(new ParticipationRequest()));

        assertThrows(ConflictException.class, () -> participationService.addParticipationRequest(userId, eventId));

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
                .status(ParticipationStatus.PENDING)
                .build();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(requestEntity));
        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(requestEntity);

        ParticipationRequestDto result = participationService.cancelRequest(userId, requestId);

        assertNotNull(result);
        assertEquals(ParticipationStatus.CANCELED.name(), result.getStatus());

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

        assertThrows(NotFoundException.class, () -> participationService.cancelRequest(userId, requestId));

        verify(requestRepository).findById(requestId);
        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }

    @Test
    void changeRequestStatus_ConfirmRequest_ShouldReturnConfirmed() {
        Long userId = 1L;
        Long eventId = 1L;

        EventRequestStatusUpdate request = new EventRequestStatusUpdate();
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
                .status(ParticipationStatus.PENDING)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.findByIdIn(anyList())).thenReturn(List.of(participationRequest));
        when(requestRepository.findByEventIdAndStatus(eventId, ParticipationStatus.CONFIRMED)).thenReturn(List.of());
        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(participationRequest);

        EventRequestStatusUpdateResult result = participationService.changeRequestStatus(userId, eventId, request);

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

        EventRequestStatusUpdate request = new EventRequestStatusUpdate();
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
                .status(ParticipationStatus.PENDING)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.findByIdIn(anyList())).thenReturn(List.of(participationRequest));
        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(participationRequest);

        EventRequestStatusUpdateResult result = participationService.changeRequestStatus(userId, eventId, request);

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

        EventRequestStatusUpdate request = new EventRequestStatusUpdate();
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
                .status(ParticipationStatus.CONFIRMED)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.findByIdIn(anyList())).thenReturn(List.of(participationRequest));

        assertThrows(ConflictException.class, () -> participationService.changeRequestStatus(userId, eventId, request));

        verify(eventRepository).findById(eventId);
        verify(requestRepository).findByIdIn(anyList());
        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }
}