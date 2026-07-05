package ru.practicum.server.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.server.dto.user.NewUserRequest;
import ru.practicum.server.dto.user.UserDto;
import ru.practicum.server.exception.ConflictException;
import ru.practicum.server.exception.NotFoundException;
import ru.practicum.server.mapper.user.UserMapper;
import ru.practicum.server.model.user.User;
import ru.practicum.server.repository.user.UserRepository;

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
            throw new ConflictException("Пользователь с почтой " + request.getEmail() + " уже существует");
        }

        User user = UserMapper.toEntity(request);
        user = userRepository.save(user);
        log.info("Пользователь создан: {}", user);
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
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        userRepository.deleteById(userId);
        log.info("Удален пользователь с ID: {}", userId);
    }
}