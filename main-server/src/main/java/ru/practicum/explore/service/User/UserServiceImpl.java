package ru.practicum.explore.service.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.dto.user.NewUserRequest;
import ru.practicum.explore.dto.user.UserDto;
import ru.practicum.explore.exception.ConflictException;
import ru.practicum.explore.exception.NotFoundException;
import ru.practicum.explore.mapper.UserMapper;
import ru.practicum.explore.model.user.User;
import ru.practicum.explore.repository.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("User with email " + request.getEmail() + " already exists");
        }

        User user = UserMapper.toEntity(request);
        user = userRepository.save(user);
        log.info("Created user: {}", user);
        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<User> users;
        if (ids != null && !ids.isEmpty()) {
            users = userRepository.findByIdIn(ids);
        } else {
            users = userRepository.findAll(pageable).getContent();
        }
        return users.stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " was not found");
        }
        userRepository.deleteById(userId);
        log.info("Deleted user with id: {}", userId);
    }
}