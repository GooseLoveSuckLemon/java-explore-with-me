package ru.practicum.explore.controller.user_controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.controller.BaseController;
import ru.practicum.explore.dto.event.EventFullDto;
import ru.practicum.explore.dto.event.NewEventDto;
import ru.practicum.explore.dto.event.UpdateEventUserRequest;
import ru.practicum.explore.service.event.EventService;

import java.util.List;

/**
 * Контроллер для работы с событиями текущего пользователя.
 *
 * <p>Все методы требуют аутентификации и работают только с событиями,
 * принадлежащими текущему пользователю.
 *
 * <p>Эндпоинты:
 * <ul>
 *   <li>POST /users/{userId}/events - создание события</li>
 *   <li>GET /users/{userId}/events - получение всех событий пользователя</li>
 *   <li>GET /users/{userId}/events/{eventId} - получение события по ID</li>
 *   <li>PATCH /users/{userId}/events/{eventId} - обновление события</li>
 * </ul>
 *
 * @author Goose
 * @version 1.0
 * @since 2026-06-26
 */

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class UserEventController extends BaseController {

        private final EventService eventService;

        /**
         * Создание нового события.
         *
         * <p>Ограничения:
         * <ul>
         *   <li>Дата события должна быть не раньше чем через 2 часа</li>
         *   <li>Событие создаётся со статусом PENDING</li>
         * </ul>
         *
         * @param userId идентификатор пользователя (из пути)
         * @param newEventDto данные для создания события
         * @return созданное событие с полной информацией
         */
        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public EventFullDto createEvent(@PathVariable Long userId,
                                        @Valid @RequestBody NewEventDto newEventDto) {
            return eventService.createEvent(userId, newEventDto);
        }

        /**
         * Получение всех событий текущего пользователя с пагинацией.
         *
         * @param userId идентификатор пользователя (из пути)
         * @param from начальный индекс (по умолчанию 0)
         * @param size размер страницы (по умолчанию 10)
         * @return список событий пользователя
         */
        @GetMapping
        public List<EventFullDto> getEvents(@PathVariable Long userId,
                                            @RequestParam(defaultValue = "0") Integer from,
                                            @RequestParam(defaultValue = "10") Integer size) {
            return eventService.getUserEvents(userId, from, size);
        }

    /**
         * Получение события по ID (только свои события).
         *
         * @param userId идентификатор пользователя (из пути)
         * @param eventId идентификатор события (из пути)
         * @return полная информация о событии
         * @throws ru.practicum.explore.exception.NotFoundException если событие не найдено
         */
        @GetMapping("/{eventId}")
        public EventFullDto getEvent(@PathVariable Long userId,
                                     @PathVariable Long eventId) {
            return eventService.getUserEvent(userId, eventId);
        }

        /**
         * Обновление события (только свои события).
         *
         * <p>Ограничения:
         * <ul>
         *   <li>Можно обновлять только события в статусе PENDING или CANCELED</li>
         *   <li>Дата события должна быть не раньше чем через 2 часа</li>
         * </ul>
         *
         * @param userId идентификатор пользователя (из пути)
         * @param eventId идентификатор события (из пути)
         * @param request данные для обновления события
         * @return обновлённое событие
         */
        @PatchMapping("/{eventId}")
        public EventFullDto updateEvent(@PathVariable Long userId,
                                        @PathVariable Long eventId,
                                        @Valid @RequestBody UpdateEventUserRequest request) {
            return eventService.updateUserEvent(userId, eventId, request);
        }
    }