package ru.practicum.server.mapper.user;

import ru.practicum.server.dto.user.NewUserRequest;
import ru.practicum.server.dto.user.UserDto;
import ru.practicum.server.dto.user.UserShortDto;
import ru.practicum.server.model.user.User;

public class UserMapper {

    public static User toEntity(NewUserRequest request) {
        return User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .build();
    }

    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public static UserShortDto toShortDto(User user) {
        if (user == null) return null;
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
