package ru.practicum.explore.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.explore.dto.user.NewUserRequest;
import ru.practicum.explore.dto.user.UserDto;
import ru.practicum.explore.exception.ConflictException;
import ru.practicum.explore.exception.NotFoundException;
import ru.practicum.explore.model.user.User;
import ru.practicum.explore.repository.user.UserRepository;
import ru.practicum.explore.service.User.UserServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_ShouldReturnUserDto() {
        NewUserRequest request = new NewUserRequest();
        request.setEmail("test@test.com");
        request.setName("Test User");

        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("Test User")
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.createUser(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@test.com", result.getEmail());
        assertEquals("Test User", result.getName());

        verify(userRepository).existsByEmail("test@test.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldThrowConflictException() {
        NewUserRequest request = new NewUserRequest();
        request.setEmail("test@test.com");
        request.setName("Test User");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.createUser(request));

        verify(userRepository).existsByEmail("test@test.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUsers_ShouldReturnUserList() {
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("Test User")
                .build();

        when(userRepository.findByIdIn(anyList())).thenReturn(List.of(user));

        List<UserDto> result = userService.getUsers(List.of(1L), 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test@test.com", result.get(0).getEmail());

        verify(userRepository).findByIdIn(List.of(1L));
    }

    @Test
    void deleteUser_ShouldDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_WithInvalidId_ShouldThrowNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.deleteUser(1L));

        verify(userRepository).existsById(1L);
        verify(userRepository, never()).deleteById(anyLong());
    }
}