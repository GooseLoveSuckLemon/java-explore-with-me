package ru.practicum.server.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.server.model.user.User;
import ru.practicum.server.repository.user.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_ShouldPersistUser() {
        User user = User.builder()
                .email("test@test.com")
                .name("Test User")
                .build();

        User saved = userRepository.save(user);

        assertNotNull(saved.getId());
        assertEquals("test@test.com", saved.getEmail());
        assertEquals("Test User", saved.getName());
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenExists() {
        User user = User.builder()
                .email("test@test.com")
                .name("Test User")
                .build();
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("test@test.com");

        assertTrue(exists);
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenNotExists() {
        boolean exists = userRepository.existsByEmail("nonexistent@test.com");
        assertFalse(exists);
    }

    @Test
    void findByIdIn_ShouldReturnUsers() {
        User user1 = User.builder()
                .email("test1@test.com")
                .name("User 1")
                .build();
        User user2 = User.builder()
                .email("test2@test.com")
                .name("User 2")
                .build();
        userRepository.save(user1);
        userRepository.save(user2);

        List<User> users = userRepository.findByIdIn(List.of(user1.getId(), user2.getId()));

        assertEquals(2, users.size());
    }

    @Test
    void findByIdIn_ShouldReturnEmpty_WhenIdsNotFound() {
        List<User> users = userRepository.findByIdIn(List.of(999L, 1000L));
        assertTrue(users.isEmpty());
    }
}