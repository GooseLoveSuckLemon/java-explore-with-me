package ru.practicum.server.service.user;

import ru.practicum.server.dto.user.NewUserRequest;
import ru.practicum.server.dto.user.UserDto;

import java.util.List;

public interface UserService {

    UserDto createUser(NewUserRequest request);

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    void deleteUser(Long userId);
}