package ru.practicum.explore.controller.admin_controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.controller.BaseController;
import ru.practicum.explore.dto.user.NewUserRequest;
import ru.practicum.explore.dto.user.UserDto;
import ru.practicum.explore.service.User.UserService;

import java.util.List;

/**
 * Контроллер для управления пользователями (Admin API).
 *
 * <p>Предоставляет административные методы для работы с пользователями системы.
 * Все эндпоинты доступны только пользователям с ролью ADMIN.
 *
 * <p>Эндпоинты:
 * <ul>
 *   <li>POST /admin/users - создание нового пользователя</li>
 *   <li>GET /admin/users - получение списка пользователей</li>
 *   <li>DELETE /admin/users/{userId} - удаление пользователя</li>
 * </ul>
 *
 * <p>Особенности:
 * <ul>
 *   <li>Email пользователя должен быть уникальным</li>
 *   <li>Поддерживается фильтрация по списку ID</li>
 *   <li>Поддерживается пагинация</li>
 * </ul>
 *
 * @author Goose
 * @version 1.0
 * @see UserService
 * @see UserDto
 * @see NewUserRequest
 * @since 2026-06-26
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController extends BaseController {

    private final UserService userService;

    /**
     * Добавление нового пользователя.
     *
     * <p>Регистрирует нового пользователя с указанными email и именем.
     * Email должен быть уникальным.
     *
     * @param request данные нового пользователя (email, имя)
     * @return созданный пользователь
     * @throws ru.practicum.explore.exception.ConflictException если email уже используется
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody NewUserRequest request) {
        return userService.createUser(request);
    }

    /**
     * Получение информации о пользователях.
     *
     * <p>Возвращает список пользователей. Если указаны ID, возвращает только их.
     * Поддерживает пагинацию.
     *
     * @param ids список ID пользователей (опционально)
     * @param from начальный индекс (по умолчанию 0)
     * @param size размер страницы (по умолчанию 10)
     * @return список пользователей
     */
    @GetMapping
    public List<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        return userService.getUsers(ids, from, size);
    }

    /**
     * Удаление пользователя.
     *
     * <p>Удаляет пользователя по ID.
     *
     * @param userId идентификатор пользователя (из пути)
     * @throws ru.practicum.explore.exception.NotFoundException если пользователь не найден
     */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }
}