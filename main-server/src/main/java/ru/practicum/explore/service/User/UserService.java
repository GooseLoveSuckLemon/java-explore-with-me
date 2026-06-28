package ru.practicum.explore.service.User;

import ru.practicum.explore.dto.user.NewUserRequest;
import ru.practicum.explore.dto.user.UserDto;

import java.util.List;

public interface UserService {

    UserDto createUser(NewUserRequest request);

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    void deleteUser(Long userId);
}