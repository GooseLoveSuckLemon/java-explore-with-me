package ru.practicum.server.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.server.controller.BaseController;
import ru.practicum.server.dto.event.EventDto;
import ru.practicum.server.dto.event.update.UpdateEventAdminRequest;
import ru.practicum.server.service.event.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.server.util.Constants.*;

/**
 * Контроллер для управления событиями (Admin API).
 *
 * <p>Предоставляет административные методы для работы с событиями.
 * Все эндпоинты доступны только пользователям с ролью ADMIN.
 *
 * <p>Эндпоинты:
 * <ul>
 *   <li>GET /admin/events - поиск событий с фильтрацией</li>
 *   <li>PATCH /admin/events/{eventId} - редактирование события</li>
 * </ul>
 *
 * <p>Особенности:
 * <ul>
 *   <li>Администратор может редактировать любые события</li>
 *   <li>Можно публиковать события (PUBLISH_EVENT)</li>
 *   <li>Можно отклонять события (REJECT_EVENT)</li>
 *   <li>Дата события должна быть не ранее чем за час от публикации</li>
 * </ul>
 *
 * @author Goose
 * @version 1.0
 * @see EventService
 * @see EventDto
 * @see UpdateEventAdminRequest
 * @since 2026-06-26
 */
@RestController
@RequestMapping(value = {"/admin/events", "/admin/events/"})
@RequiredArgsConstructor
public class AdminEventController extends BaseController {

    private final EventService eventService;

    /**
     * Поиск событий с фильтрацией.
     *
     * <p>Возвращает полную информацию о событиях, соответствующих фильтрам.
     * Поддерживает фильтрацию по:
     * <ul>
     *   <li>пользователям (users)</li>
     *   <li>состояниям (states)</li>
     *   <li>категориям (categories)</li>
     *   <li>диапазону дат (rangeStart, rangeEnd)</li>
     * </ul>
     *
     * @param users список ID пользователей (опционально)
     * @param states список состояний (опционально)
     * @param categories список ID категорий (опционально)
     * @param rangeStart начало диапазона дат (опционально)
     * @param rangeEnd конец диапазона дат (опционально)
     * @param from начальный индекс (по умолчанию 0)
     * @param size размер страницы (по умолчанию 10)
     * @return список событий с полной информацией
     */
    @GetMapping
    public List<EventDto> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = DEFAULT_FROM) Integer from,
            @RequestParam(defaultValue = DEFAULT_SIZE) Integer size) {
        return eventService.getEventsByAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    /**
     * Редактирование события администратором.
     *
     * <p>Позволяет изменить данные события и его статус.
     *
     * <p>Ограничения:
     * <ul>
     *   <li>Дата начала должна быть не ранее чем за час от публикации</li>
     *   <li>Событие можно опубликовать только в статусе PENDING</li>
     *   <li>Событие можно отклонить только если оно ещё не опубликовано</li>
     * </ul>
     *
     * @param eventId идентификатор события (из пути)
     * @param request данные для обновления события
     * @return обновлённое событие
     * @throws ru.practicum.server.exception.NotFoundException если событие не найдено
     * @throws ru.practicum.server.exception.ConflictException если нарушены правила редактирования
     */
    @PatchMapping("/{eventId}")
    public EventDto updateEvent(@PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventAdminRequest request) {
        return eventService.updateEventByAdmin(eventId, request);
    }

    /**
     * Удаление события администратором.
     *
     * <p>Полностью удаляет событие из системы.
     * В отличие от пользовательского удаления (CANCELED), администратор
     * может удалить событие безвозвратно независимо от его статуса.
     *
     * <p>После удаления:
     * <ul>
     *   <li>Событие полностью удаляется из базы данных</li>
     *   <li>Все связанные данные (участники, запросы) также удаляются</li>
     *   <li>Восстановление невозможно</li>
     * </ul>
     *
     * @param eventId идентификатор события (из пути)
     * @throws ru.practicum.server.exception.NotFoundException если событие не найдено
     * @throws ru.practicum.server.exception.ConflictException если событие уже опубликовано
     *         и не может быть удалено
     */
    @DeleteMapping("/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Long eventId) {
        eventService.deleteEvent(eventId);
    }
}
