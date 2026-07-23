package ru.practicum.server.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.server.controller.BaseController;
import ru.practicum.server.dto.rating.EventRatingDto;
import ru.practicum.server.dto.rating.EventRatingStatsDto;
import ru.practicum.server.dto.rating.UserRatingDto;
import ru.practicum.server.exception.ConflictException;
import ru.practicum.server.exception.NotFoundException;
import ru.practicum.server.service.rating.RatingService;

import java.util.List;
import java.util.Map;

/**
 * Контроллер для управления рейтингами событий (User API).
 * <p>
 * Предоставляет методы для работы с рейтингами событий от имени авторизованного пользователя.
 * Все эндпоинты доступны только аутентифицированным пользователям.
 * </p>
 *
 * <p>Эндпоинты:
 * <ul>
 *   <li>POST /users/{userId}/ratings/{eventId} - создание или обновление оценки</li>
 *   <li>DELETE /users/{userId}/ratings/{eventId} - удаление оценки</li>
 *   <li>GET /users/{userId}/ratings/{eventId} - получение оценки пользователя</li>
 *   <li>GET /users/{userId}/ratings/{eventId}/stats - статистика рейтинга события</li>
 *   <li>GET /users/{userId}/ratings/user-rating - рейтинг пользователя как автора</li>
 *   <li>GET /users/{userId}/ratings/user-ratings - рейтинги нескольких пользователей</li>
 * </ul>
 * </p>
 *
 * @author Goose
 * @version 1.0
 * @see RatingService
 * @since 2026-07-22
 */
@RestController
@RequestMapping("/users/{userId}/ratings")
@RequiredArgsConstructor
public class UserRatingController extends BaseController {

    private final RatingService ratingService;


     /**
     * Добавляет или обновляет оценку пользователя для события.
     * <p>
     * Позволяет пользователю поставить лайк или дизлайк событию.
     * Если оценка уже существует, она будет обновлена.
     * </p>
     *
     * <p>Ограничения:
     * <ul>
     *   <li>Нельзя оценивать собственное событие</li>
     *   <li>Можно оценивать только опубликованные события</li>
     * </ul>
     * </p>
     *
     * @param userId  идентификатор пользователя (из пути)
     * @return информация о созданной или обновленной оценке
     * @throws NotFoundException если пользователь или событие не найдены
     * @throws ConflictException если пользователь является автором события
     */
    @GetMapping("/user-rating")
    public UserRatingDto getUserRating(@PathVariable Long userId) {
        return ratingService.getUserRating(userId);
    }

    /**
     * Получает рейтинги нескольких пользователей как авторов событий.
     * <p>
     * Возвращает Map, где ключ - ID пользователя, значение - его рейтинг.
     * Используется для массового получения рейтингов.
     * </p>
     *
     * @param userIds список идентификаторов пользователей (параметр запроса)
     * @return Map с рейтингами пользователей
     */
    @GetMapping("/user-ratings")
    public Map<Long, UserRatingDto> getUserRatings(@RequestParam List<Long> userIds) {
        return ratingService.getUserRatings(userIds);
    }

     /**
     * Получает оценку пользователя для события.
     * <p>
     * Возвращает информацию о текущей оценке (лайк или дизлайк), которую
     * пользователь поставил для указанного события.
     * </p>
     *
     * <p>Возвращаемая информация включает:
     * <ul>
     *   <li>ID оценки</li>
     *   <li>ID события</li>
     *   <li>ID пользователя</li>
     *   <li>Тип оценки (true - лайк, false - дизлайк)</li>
     *   <li>Дату и время создания оценки</li>
     *   <li>Дату и время последнего обновления оценки</li>
     * </ul>
     * </p>
     *
     * <p>Используется для:
     * <ul>
     *   <li>Отображения текущей оценки пользователя в UI</li>
     *   <li>Проверки, оценивал ли пользователь событие</li>
     *   <li>Определения текущего типа оценки перед обновлением</li>
     * </ul>
     * </p>
     *
     * @param userId  идентификатор пользователя, чья оценка запрашивается (из пути)
     * @param eventId идентификатор события, для которого запрашивается оценка (из пути)
     * @return {@link EventRatingDto} с информацией об оценке пользователя
     * @throws NotFoundException если оценка для указанного пользователя и события не найдена
     * @throws NotFoundException если пользователь или событие не найдены
     */
    @GetMapping("/{eventId}")
    public EventRatingDto getRating(@PathVariable Long userId, @PathVariable Long eventId) {
        return ratingService.getRating(userId, eventId);
    }

     /**
     * Получает статистику рейтинга события.
     * <p>
     * Возвращает количество лайков, дизлайков, общее количество голосов
     * и рейтинг в процентах.
     * </p>
     *
     * @param userId  идентификатор пользователя (из пути, не используется)
     * @param eventId идентификатор события (из пути)
     * @return статистика рейтинга события
     * @throws NotFoundException если событие не найдено
     */
    @GetMapping("/{eventId}/stats")
    public EventRatingStatsDto getEventRatingStats(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        return ratingService.getEventRatingStats(eventId);
    }

     /**
     * Добавление или обновление оценки события.
     * <p>
     * Создает новую оценку или обновляет существующую для указанного события.
     * </p>
     *
     * @param userId  идентификатор пользователя (из пути)
     * @param eventId идентификатор события (из пути)
     * @param isLike  тип оценки: true - лайк, false - дизлайк
     * @return информация об оценке
     * @throws NotFoundException если пользователь или событие не найдены
     * @throws ConflictException если пользователь является автором события
     */
    @PostMapping("/{eventId}")
    public EventRatingDto addOrUpdateRating(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestParam Boolean isLike) {
        return ratingService.addOrUpdateRating(userId, eventId, isLike);
    }

     /**
     * Удаляет оценку пользователя для события.
     * <p>
     * Позволяет пользователю удалить свою оценку (лайк или дизлайк) для указанного события.
     * После удаления оценка полностью удаляется из системы, и пользователь может
     * повторно оценить событие позже.
     * </p>
     *
     * <p>Особенности:
     * <ul>
     *   <li>Удалить можно только свою оценку (по userId из пути)</li>
     *   <li>После удаления можно повторно поставить оценку</li>
     *   <li>При удалении обновляется статистика рейтинга события</li>
     * </ul>
     * </p>
     *
     * @param userId  идентификатор пользователя, удаляющего оценку (из пути)
     * @param eventId идентификатор события, для которого удаляется оценка (из пути)
     * @throws NotFoundException если оценка для указанного пользователя и события не найдена
     * @throws NotFoundException если пользователь или событие не найдены
     */
    @DeleteMapping("/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRating(@PathVariable Long userId, @PathVariable Long eventId) {
        ratingService.deleteRating(userId, eventId);
    }
}