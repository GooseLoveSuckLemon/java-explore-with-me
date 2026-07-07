package ru.practicum.server.service.user;

import ru.practicum.server.dto.user.NewUserRequest;
import ru.practicum.server.dto.user.UserDto;

import java.util.List;

/**
 * Сервис для управления пользователями.
 * Предоставляет методы для создания, получения и удаления пользователей.
 *
 * @author Goose
 * @version 1.0
 * @since 2026-07-08
 */
public interface UserService {

    /**
     * Создает нового пользователя.
     *
     * @param request данные для создания пользователя
     * @return созданный пользователь в виде DTO
     * @throws ru.practicum.server.exception.ConflictException если пользователь с таким email уже существует
     */
    UserDto createUser(NewUserRequest request);

    /**
     * Получает список пользователей с возможностью фильтрации по ID.
     *
     * @param ids  список ID пользователей для фильтрации (может быть null)
     * @param from начальная позиция для пагинации
     * @param size количество записей на странице
     * @return список пользователей в виде DTO
     */
    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    /**
     * Удаляет пользователя по ID.
     *
     * @param userId ID пользователя для удаления
     * @throws ru.practicum.server.exception.NotFoundException если пользователь не найден
     */
    void deleteUser(Long userId);
}