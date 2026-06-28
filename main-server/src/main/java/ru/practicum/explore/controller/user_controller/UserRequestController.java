package ru.practicum.explore.controller.user_controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.controller.BaseController;
import ru.practicum.explore.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.explore.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.explore.dto.participation.ParticipationRequestDto;
import ru.practicum.explore.service.request.RequestService;

import java.util.List;

/**
 * Контроллер для работы с заявками на участие (Private API).
 *
 * <p>Все методы требуют аутентификации.
 *
 * <p>Эндпоинты:
 * <ul>
 *   <li>GET /users/{userId}/requests - все заявки пользователя</li>
 *   <li>POST /users/{userId}/requests - создание заявки</li>
 *   <li>PATCH /users/{userId}/requests/{requestId}/cancel - отмена заявки</li>
 *   <li>GET /users/{userId}/events/{eventId}/requests - заявки на событие</li>
 *   <li>PATCH /users/{userId}/events/{eventId}/requests - изменение статуса заявок</li>
 * </ul>
 *
 * @author Goose
 * @version 1.0
 * @since 2026-06-26
 */
@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
public class UserRequestController extends BaseController {

    private final RequestService requestService;

    /**
     * Получение всех заявок текущего пользователя на участие в чужих событиях.
     *
     * @param userId идентификатор пользователя (из пути)
     * @return список заявок пользователя
     */
    @GetMapping("/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {
        return requestService.getUserRequests(userId);
    }

    /**
     * Создание заявки на участие в событии.
     *
     * <p>Ограничения:
     * <ul>
     *   <li>Нельзя создать повторную заявку</li>
     *   <li>Нельзя участвовать в своём событии</li>
     *   <li>Нельзя участвовать в неопубликованном событии</li>
     * </ul>
     *
     * @param userId идентификатор пользователя (из пути)
     * @param eventId идентификатор события (из параметров запроса)
     * @return созданная заявка
     */
    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addParticipationRequest(@PathVariable Long userId,
                                                           @RequestParam Long eventId) {
        return requestService.addParticipationRequest(userId, eventId);
    }

    /**
     * Отмена своей заявки на участие.
     *
     * @param userId идентификатор пользователя (из пути)
     * @param requestId идентификатор заявки (из пути)
     * @return отменённая заявка
     */
    @PatchMapping("/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId,
                                                 @PathVariable Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }

    /**
     * Получение всех заявок на участие в событии (только для организатора).
     *
     * @param userId идентификатор пользователя (из пути)
     * @param eventId идентификатор события (из пути)
     * @return список заявок на участие в событии
     */
    @GetMapping("/events/{eventId}/requests")
    public List<ParticipationRequestDto> getEventParticipants(@PathVariable Long userId,
                                                              @PathVariable Long eventId) {
        return requestService.getEventParticipants(userId, eventId);
    }

    /**
     * Изменение статуса заявок на участие в событии (подтвердить/отклонить).
     *
     * <p>Ограничения:
     * <ul>
     *   <li>Можно изменять только заявки в статусе PENDING</li>
     *   <li>При достижении лимита участников остальные заявки отклоняются</li>
     *   <li>Если лимит 0 или отключена пре-модерация, заявки подтверждаются автоматически</li>
     * </ul>
     *
     * @param userId идентификатор пользователя (из пути)
     * @param eventId идентификатор события (из пути)
     * @param request запрос с IDs заявок и новым статусом
     * @return результат изменения статусов
     */
    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult changeRequestStatus(@PathVariable Long userId,
                                                              @PathVariable Long eventId,
                                                              @RequestBody EventRequestStatusUpdateRequest request) {
        return requestService.changeRequestStatus(userId, eventId, request);
    }
}