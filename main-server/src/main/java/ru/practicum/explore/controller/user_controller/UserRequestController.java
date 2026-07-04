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
 * Контроллер для управления запросами на участие в событиях (User API).
 *
 * <p>Предоставляет методы для работы с запросами на участие от имени
 * авторизованного пользователя. Все эндпоинты доступны только
 * аутентифицированным пользователям.
 *
 * <p>Эндпоинты:
 * <ul>
 *   <li>GET /users/{userId}/requests - получение запросов пользователя</li>
 *   <li>POST /users/{userId}/requests - создание запроса на участие</li>
 *   <li>PATCH /users/{userId}/requests/{requestId}/cancel - отмена запроса</li>
 *   <li>GET /users/{userId}/events/{eventId}/requests - получение запросов на событие</li>
 *   <li>PATCH /users/{userId}/events/{eventId}/requests - изменение статуса запросов</li>
 * </ul>
 *
 * <p>Особенности работы с запросами:
 * <ul>
 *   <li>Пользователь может создать запрос на участие в событии</li>
 *   <li>Пользователь может отменить свой запрос до его подтверждения</li>
 *   <li>Инициатор события может подтверждать или отклонять запросы</li>
 *   <li>Количество участников ограничено лимитом события</li>
 *   <li>При достижении лимита автоматически отклоняются оставшиеся запросы</li>
 * </ul>
 *
 * <p>Статусы запросов:
 * <ul>
 *   <li><b>PENDING</b> - ожидает подтверждения</li>
 *   <li><b>CONFIRMED</b> - подтверждён</li>
 *   <li><b>REJECTED</b> - отклонён</li>
 *   <li><b>CANCELED</b> - отменён пользователем</li>
 * </ul>
 *
 * @author Goose
 * @version 1.0
 * @since 2026-07-01
 */
@RestController
@RequestMapping(value = {"/users/{userId}", "/users/{userId}/"})
@RequiredArgsConstructor
public class UserRequestController extends BaseController {

    private final RequestService requestService;

     /**
     * Получение всех запросов пользователя на участие в событиях.
     *
     * <p>Возвращает список всех запросов, созданных указанным пользователем.
     * Запросы сортируются по дате создания в порядке убывания.
     *
     * <p>Возвращаемая информация включает:
     * <ul>
     *   <li>ID запроса</li>
     *   <li>ID события</li>
     *   <li>ID пользователя</li>
     *   <li>Статус запроса</li>
     *   <li>Дату создания</li>
     * </ul>
     *
     * @param userId идентификатор пользователя (из пути)
     * @return список запросов пользователя
     * @throws ru.practicum.explore.exception.NotFoundException если пользователь не найден
     */
     @GetMapping("/requests")
     public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {
         return requestService.getUserRequests(userId);
     }

     /**
     * Создание запроса на участие в событии.
     *
     * <p>Позволяет пользователю подать заявку на участие в событии.
     * Создание запроса возможно при соблюдении следующих условий:
     *
     * <p>Ограничения:
     * <ul>
     *   <li>Пользователь не является инициатором события</li>
     *   <li>Событие опубликовано (статус PUBLISHED)</li>
     *   <li>Не превышен лимит участников события</li>
     *   <li>Пользователь уже не подал заявку на это событие</li>
     *   <li>Для событий без премодерации запрос сразу становится CONFIRMED</li>
     * </ul>
     *
     * <p>Особенности:
     * <ul>
     *   <li>Если для события не требуется модерация (requestModeration = false),
     *       запрос сразу подтверждается</li>
     *   <li>Если лимит участников (participantLimit) равен 0, модерация отключена</li>
     *   <li>При достижении лимита все оставшиеся PENDING запросы автоматически
     *       отклоняются</li>
     * </ul>
     *
     * @param userId идентификатор пользователя (из пути)
     * @param eventId идентификатор события, на которое подаётся заявка (обязательный параметр)
     * @return созданный запрос с текущим статусом
     * @throws IllegalArgumentException если eventId не передан
     * @throws ru.practicum.explore.exception.NotFoundException если:
     *         <ul>
     *           <li>Пользователь не найден</li>
     *           <li>Событие не найдено</li>
     *         </ul>
     * @throws ru.practicum.explore.exception.ConflictException если:
     *         <ul>
     *           <li>Пользователь является инициатором события</li>
     *           <li>Событие не опубликовано</li>
     *           <li>Достигнут лимит участников</li>
     *           <li>Пользователь уже подал заявку</li>
     *         </ul>
     */
     @PostMapping("/requests")
     @ResponseStatus(HttpStatus.CREATED)
     public ParticipationRequestDto addParticipationRequest(@PathVariable Long userId,
                                                            @RequestParam(required = false) Long eventId) {
         if (eventId == null) {
             throw new IllegalArgumentException("eventId parameter is required");
         }
         return requestService.addParticipationRequest(userId, eventId);
     }

     /**
     * Отмена запроса на участие пользователем.
     *
     * <p>Позволяет пользователю отменить свой запрос на участие в событии.
     * Запрос можно отменить только если он ещё не подтверждён.
     *
     * <p>Особенности:
     * <ul>
     *   <li>После отмены запроса его статус становится CANCELED</li>
     *   <li>Если событие имеет лимит участников, освободившееся место может
     *       быть занято другим пользователем (только если включена модерация)</li>
     *   <li>Инициатор события не может отменить запрос через этот метод -
     *       для этого используется изменение статуса</li>
     * </ul>
     *
     * @param userId идентификатор пользователя (из пути)
     * @param requestId идентификатор запроса на участие (из пути)
     * @return отменённый запрос со статусом CANCELED
     * @throws ru.practicum.explore.exception.NotFoundException если:
     *         <ul>
     *           <li>Пользователь не найден</li>
     *           <li>Запрос не найден</li>
     *         </ul>
     * @throws ru.practicum.explore.exception.ConflictException если:
     *         <ul>
     *           <li>Запрос уже подтверждён (CONFIRMED)</li>
     *           <li>Запрос уже отклонён (REJECTED)</li>
     *           <li>Пользователь не является владельцем запроса</li>
     *         </ul>
     */
     @PatchMapping("/requests/{requestId}/cancel")
     public ParticipationRequestDto cancelRequest(@PathVariable Long userId,
                                                  @PathVariable Long requestId) {
         return requestService.cancelRequest(userId, requestId);
     }

     /**
     * Получение списка запросов на участие в событии пользователя.
     *
     * <p>Возвращает все запросы на участие в указанном событии.
     * Доступно только инициатору события.
     *
     * <p>Используется для просмотра всех желающих принять участие в событии
     * перед принятием решения о подтверждении или отклонении заявок.
     *
     * @param userId идентификатор пользователя-инициатора (из пути)
     * @param eventId идентификатор события (из пути)
     * @return список запросов на участие в событии
     * @throws ru.practicum.explore.exception.NotFoundException если:
     *         <ul>
     *           <li>Пользователь не найден</li>
     *           <li>Событие не найдено</li>
     *         </ul>
     * @throws ru.practicum.explore.exception.ConflictException если пользователь
     *         не является инициатором события
     */
     @GetMapping("/events/{eventId}/requests")
     public List<ParticipationRequestDto> getEventParticipants(@PathVariable Long userId,
                                                               @PathVariable Long eventId) {
         return requestService.getEventParticipants(userId, eventId);
     }

     /**
     * Изменение статуса запросов на участие в событии.
     *
     * <p>Позволяет инициатору события подтвердить или отклонить запросы
     * на участие. Доступно только для событий, созданных пользователем.
     *
     * <p>Действия:
     * <ul>
     *   <li><b>CONFIRMED</b> - подтверждение запроса.
     *       <br>Условия:
     *       <ul>
     *         <li>Статус запроса должен быть PENDING</li>
     *         <li>Не достигнут лимит участников события</li>
     *         <li>Количество подтверждённых запросов не превышает лимит</li>
     *       </ul>
     *   </li>
     *   <li><b>REJECTED</b> - отклонение запроса.
     *       <br>Условия:
     *       <ul>
     *         <li>Статус запроса должен быть PENDING</li>
     *         <li>Отклонить можно только если событие ещё не заполнено</li>
     *       </ul>
     *   </li>
     * </ul>
     *
     * <p>Особенности:
     * <ul>
     *   <li>Можно подтвердить или отклонить несколько запросов одновременно</li>
     *   <li>При подтверждении запросов проверяется лимит участников</li>
     *   <li>Если лимит достигнут, оставшиеся PENDING запросы автоматически
     *       отклоняются</li>
     *   <li>Нельзя подтвердить запрос, если событие уже заполнено</li>
     *   <li>При частичном успехе возвращается список подтверждённых и
     *       отклонённых запросов</li>
     * </ul>
     *
     * @param userId идентификатор пользователя-инициатора (из пути)
     * @param eventId идентификатор события (из пути)
     * @param request объект с запросами и действием (статусом для установки)
     * @return результат операции с двумя списками:
     *         <ul>
     *           <li>confirmedRequests - подтверждённые запросы</li>
     *           <li>rejectedRequests - отклонённые запросы</li>
     *         </ul>
     * @throws ru.practicum.explore.exception.NotFoundException если:
     *         <ul>
     *           <li>Пользователь не найден</li>
     *           <li>Событие не найдено</li>
     *           <li>Один из запросов не найден</li>
     *         </ul>
     * @throws ru.practicum.explore.exception.ConflictException если:
     *         <ul>
     *           <li>Пользователь не является инициатором события</li>
     *           <li>Статус запроса не позволяет выполнить действие
     *               (не PENDING)</li>
     *           <li>Превышен лимит участников</li>
     *           <li>Событие уже заполнено (для подтверждения)</li>
     *         </ul>
     */
     @PatchMapping("/events/{eventId}/requests")
     public EventRequestStatusUpdateResult changeRequestStatus(@PathVariable Long userId,
                                                               @PathVariable Long eventId,
                                                               @RequestBody EventRequestStatusUpdateRequest request) {
         return requestService.changeRequestStatus(userId, eventId, request);
     }
}