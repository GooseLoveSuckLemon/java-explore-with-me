package ru.practicum.explore.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.explore.model.category.Category;
import ru.practicum.explore.model.event.Event;
import ru.practicum.explore.model.event.EventState;
import ru.practicum.explore.model.location.EventLocation;
import ru.practicum.explore.model.user.User;
import ru.practicum.explore.repository.event.EventRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Long userId;
    private Long categoryId;

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
        categoryId = category.getId();

        entityManager.flush();
    }

    @Test
    void save_ShouldPersistEvent() {
        Event event = createEvent();

        Event saved = eventRepository.save(event);

        assertNotNull(saved.getId());
        assertEquals("Тестовое событие", saved.getTitle());
        assertEquals(EventState.PENDING, saved.getState());
    }

    @Test
    void findByInitiatorId_ShouldReturnEvents() {
        Event event = createEvent();
        entityManager.persist(event);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);
        var events = eventRepository.findByInitiatorId(userId, pageable);

        assertFalse(events.isEmpty());
        assertEquals(1, events.size());
    }

    @Test
    void findByState_ShouldReturnEvents() {
        Event event = createEvent();
        event.setState(EventState.PUBLISHED);
        entityManager.persist(event);
        entityManager.flush();

        var events = eventRepository.findByState(EventState.PUBLISHED);

        assertFalse(events.isEmpty());
        assertEquals(EventState.PUBLISHED, events.get(0).getState());
    }

    private Event createEvent() {
        User user = entityManager.find(User.class, userId);
        Category category = entityManager.find(Category.class, categoryId);

        return Event.builder()
                .annotation("Тестовое событие")
                .category(category)
                .createdOn(LocalDateTime.now())
                .description("Описание тестового события")
                .eventDate(LocalDateTime.now().plusHours(3))
                .initiator(user)
                .eventLocation(EventLocation.builder().lat(55.754167f).lon(37.62f).build())
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .state(EventState.PENDING)
                .title("Тестовое событие")
                .build();
    }
}