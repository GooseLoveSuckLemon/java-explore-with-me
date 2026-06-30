package ru.practicum.explore.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.explore.model.category.Category;
import ru.practicum.explore.model.event.Event;
import ru.practicum.explore.model.event.EventState;
import ru.practicum.explore.model.location.EventLocation;
import ru.practicum.explore.model.participationRequest.ParticipationRequest;
import ru.practicum.explore.model.participationRequest.RequestStatus;
import ru.practicum.explore.model.user.User;
import ru.practicum.explore.repository.participation.ParticipationRequestRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ParticipationRequestRepositoryTest {

    @Autowired
    private ParticipationRequestRepository requestRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Long userId;
    private Long eventId;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .email("test@test.com")
                .name("Test User")
                .build();
        entityManager.persist(user);
        userId = user.getId();

        Category category = Category.builder().name("Концерты").build();
        entityManager.persist(category);

        Event event = Event.builder()
                .annotation("Тестовое событие")
                .category(category)
                .createdOn(LocalDateTime.now())
                .description("Описание")
                .eventDate(LocalDateTime.now().plusHours(3))
                .initiator(user)
                .eventLocation(EventLocation.builder().lat(55.754167f).lon(37.62f).build())
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .title("Тестовое событие")
                .build();
        entityManager.persist(event);
        eventId = event.getId();

        entityManager.flush();
    }

    @Test
    void save_ShouldPersistRequest() {
        User user = entityManager.find(User.class, userId);
        Event event = entityManager.find(Event.class, eventId);

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(RequestStatus.PENDING)
                .build();

        ParticipationRequest saved = requestRepository.save(request);

        assertNotNull(saved.getId());
        assertEquals(RequestStatus.PENDING, saved.getStatus());
    }

    @Test
    void findByRequesterId_ShouldReturnRequests() {
        User user = entityManager.find(User.class, userId);
        Event event = entityManager.find(Event.class, eventId);

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(RequestStatus.PENDING)
                .build();
        entityManager.persist(request);
        entityManager.flush();

        var requests = requestRepository.findByRequesterId(userId);

        assertEquals(1, requests.size());
    }

    @Test
    void findByEventIdAndStatus_ShouldReturnRequests() {
        User user = entityManager.find(User.class, userId);
        Event event = entityManager.find(Event.class, eventId);

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(RequestStatus.PENDING)
                .build();
        entityManager.persist(request);
        entityManager.flush();

        var requests = requestRepository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);

        assertEquals(1, requests.size());
    }

    @Test
    void existsByRequesterIdAndEventId_ShouldReturnTrue_WhenExists() {
        User user = entityManager.find(User.class, userId);
        Event event = entityManager.find(Event.class, eventId);

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(RequestStatus.PENDING)
                .build();
        entityManager.persist(request);
        entityManager.flush();

        boolean exists = requestRepository.existsByRequesterIdAndEventId(userId, eventId);

        assertTrue(exists);
    }
}