package ru.practicum.explore.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.explore.dto.event.EventFullDto;
import ru.practicum.explore.dto.event.NewEventDto;
import ru.practicum.explore.dto.event.UpdateEventAdminRequest;
import ru.practicum.explore.dto.event.UpdateEventUserRequest;
import ru.practicum.explore.exception.ConflictException;
import ru.practicum.explore.exception.NotFoundException;
import ru.practicum.explore.model.category.Category;
import ru.practicum.explore.model.event.Event;
import ru.practicum.explore.model.event.EventState;
import ru.practicum.explore.model.user.User;
import ru.practicum.explore.repository.category.CategoryRepository;
import ru.practicum.explore.repository.event.EventRepository;
import ru.practicum.explore.repository.participation.ParticipationRequestRepository;
import ru.practicum.explore.repository.user.UserRepository;
import ru.practicum.explore.service.event.EventServiceImpl;
import ru.practicum.stats.client.StatsClient;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ParticipationRequestRepository requestRepository;

    @Mock
    private StatsClient statsClient;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void createEvent_ShouldReturnEventFullDto() {
        Long userId = 1L;
        NewEventDto dto = new NewEventDto();
        dto.setAnnotation("Тестовое событие для проверки валидации");
        dto.setCategory(1L);
        dto.setDescription("Описание события длиной более 20 символов");
        dto.setEventDate(LocalDateTime.now().plusHours(3));
        dto.setTitle("Тестовое событие");
        dto.setPaid(false);
        dto.setParticipantLimit(10);
        dto.setRequestModeration(true);

        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .name("Test User")
                .build();
        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            event.setId(1L);
            return event;
        });

        EventFullDto result = eventService.createEvent(userId, dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Тестовое событие", result.getTitle());
        assertEquals(EventState.PENDING.name(), result.getState());

        verify(userRepository).findById(userId);
        verify(categoryRepository).findById(1L);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createEvent_WithUserNotFound_ShouldThrowNotFoundException() {
        Long userId = 1L;
        NewEventDto dto = new NewEventDto();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> eventService.createEvent(userId, dto));

        verify(userRepository).findById(userId);
        verify(categoryRepository, never()).findById(anyLong());
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void createEvent_WithCategoryNotFound_ShouldThrowNotFoundException() {
        Long userId = 1L;
        NewEventDto dto = new NewEventDto();
        dto.setCategory(1L);

        User user = User.builder().id(userId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> eventService.createEvent(userId, dto));

        verify(userRepository).findById(userId);
        verify(categoryRepository).findById(1L);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void createEvent_WithInvalidDate_ShouldThrowIllegalArgumentException() {
        Long userId = 1L;
        NewEventDto dto = new NewEventDto();
        dto.setCategory(1L);
        dto.setEventDate(LocalDateTime.now().minusHours(1));
        dto.setAnnotation("Тестовое событие для проверки валидации");
        dto.setDescription("Описание события длиной более 20 символов");
        dto.setTitle("Тестовое событие");

        User user = User.builder().id(userId).build();
        Category category = Category.builder().id(1L).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(userId, dto));

        verify(userRepository).findById(userId);
        verify(categoryRepository).findById(1L);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void updateUserEvent_WithPublishedEvent_ShouldThrowConflictException() {
        Long userId = 1L;
        Long eventId = 1L;
        UpdateEventUserRequest request = new UpdateEventUserRequest();
        request.setAnnotation("Обновленное событие для проверки валидации");

        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .name("Test User")
                .build();

        // Создаем событие со статусом PUBLISHED
        Event event = Event.builder()
                .id(eventId)
                .annotation("Тестовое событие для проверки валидации")
                .category(category)
                .initiator(user)
                .state(EventState.PUBLISHED)
                .title("Тестовое событие")
                .description("Описание события длиной более 20 символов")
                .build();

        // Логируем статус для отладки
        System.out.println("Event state: " + event.getState());
        System.out.println("Event state == PUBLISHED: " + (event.getState() == EventState.PUBLISHED));

        when(eventRepository.findByIdAndInitiatorId(eq(eventId), eq(userId)))
                .thenReturn(Optional.of(event));

        // Проверяем, что действительно выбрасывается ConflictException
        ConflictException exception = assertThrows(ConflictException.class,
                () -> eventService.updateUserEvent(userId, eventId, request));

        assertEquals("Изменить можно только ожидающие или отмененные события.", exception.getMessage());

        verify(eventRepository).findByIdAndInitiatorId(eventId, userId);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void updateUserEvent_WithInvalidDate_ShouldThrowIllegalArgumentException() {
        Long userId = 1L;
        Long eventId = 1L;
        UpdateEventUserRequest request = new UpdateEventUserRequest();
        request.setEventDate(LocalDateTime.now().minusHours(1));

        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .name("Test User")
                .build();

        Event event = Event.builder()
                .id(eventId)
                .annotation("Тестовое событие для проверки валидации")
                .category(category)
                .initiator(user)
                .state(EventState.PENDING)
                .title("Тестовое событие")
                .description("Описание события длиной более 20 символов")
                .build();

        when(eventRepository.findByIdAndInitiatorId(eq(eventId), eq(userId)))
                .thenReturn(Optional.of(event));

        assertThrows(IllegalArgumentException.class, () -> eventService.updateUserEvent(userId, eventId, request));

        verify(eventRepository).findByIdAndInitiatorId(eventId, userId);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void updateEventByAdmin_PublishEvent_ShouldReturnPublished() {
        Long eventId = 1L;
        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction("PUBLISH_EVENT");

        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("Test User")
                .build();

        Event event = Event.builder()
                .id(eventId)
                .annotation("Тестовое событие")
                .category(category)
                .initiator(user)
                .state(EventState.PENDING)
                .title("Тестовое событие")
                .description("Описание")
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        EventFullDto result = eventService.updateEventByAdmin(eventId, request);

        assertNotNull(result);
        assertEquals(EventState.PUBLISHED.name(), result.getState());
    }

    @Test
    void updateEventByAdmin_PublishEvent_WithInvalidState_ShouldThrowConflictException() {
        Long eventId = 1L;
        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction("PUBLISH_EVENT");

        Event event = Event.builder()
                .id(eventId)
                .state(EventState.PUBLISHED)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThrows(ConflictException.class, () -> eventService.updateEventByAdmin(eventId, request));

        verify(eventRepository).findById(eventId);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void updateEventByAdmin_RejectEvent_ShouldReturnCanceled() {
        Long eventId = 1L;
        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction("REJECT_EVENT");

        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("Test User")
                .build();

        Event event = Event.builder()
                .id(eventId)
                .annotation("Тестовое событие")
                .category(category)
                .initiator(user)
                .state(EventState.PENDING)
                .title("Тестовое событие")
                .description("Описание")
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        EventFullDto result = eventService.updateEventByAdmin(eventId, request);

        assertNotNull(result);
        assertEquals(EventState.CANCELED.name(), result.getState());

        verify(eventRepository).findById(eventId);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void updateEventByAdmin_RejectEvent_WithPublishedState_ShouldThrowConflictException() {
        Long eventId = 1L;
        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction("REJECT_EVENT");

        Event event = Event.builder()
                .id(eventId)
                .state(EventState.PUBLISHED)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThrows(ConflictException.class, () -> eventService.updateEventByAdmin(eventId, request));

        verify(eventRepository).findById(eventId);
        verify(eventRepository, never()).save(any(Event.class));
    }
}