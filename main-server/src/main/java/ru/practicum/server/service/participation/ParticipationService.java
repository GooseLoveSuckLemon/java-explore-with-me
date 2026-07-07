package ru.practicum.server.service.participation;

import ru.practicum.server.dto.event.update.EventRequestStatusUpdate;
import ru.practicum.server.dto.event.update.EventRequestStatusUpdateResult;
import ru.practicum.server.dto.participation.ParticipationRequestDto;

import java.util.List;

/**
 * Сервис для управления запросами на участие в событиях.
 * Предоставляет методы для создания, отмены и изменения статуса запросов на участие.
 *
 * @author Goose
 * @version 1.0
 * @since 2026-07-08
 */
public interface ParticipationService {

     /**
     * Получает все запросы на участие пользователя.
     *
     * @param userId ID пользователя
     * @return список запросов пользователя в виде DTO
     * @throws ru.practicum.server.exception.NotFoundException если пользователь не найден
     */
    List<ParticipationRequestDto> getUserRequests(Long userId);

     /**
     * Создает запрос на участие в событии от имени пользователя.
     *
     * @param userId  ID пользователя, подающего запрос
     * @param eventId ID события для участия
     * @return созданный запрос в виде DTO
     * @throws ru.practicum.server.exception.NotFoundException если пользователь или событие не найдены
     * @throws ru.practicum.server.exception.ConflictException если запрос уже существует или событие недоступно
     */
    ParticipationRequestDto addParticipationRequest(Long userId, Long eventId);

     /**
     * Отменяет запрос на участие пользователя.
     *
     * @param userId    ID пользователя
     * @param requestId ID запроса для отмены
     * @return отмененный запрос в виде DTO
     * @throws ru.practicum.server.exception.NotFoundException если запрос не найден
     * @throws ru.practicum.server.exception.ConflictException если запрос уже подтвержден
     */
    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

     /**
     * Получает список запросов на участие в событии для инициатора события.
     *
     * @param userId  ID пользователя-инициатора события
     * @param eventId ID события
     * @return список запросов на участие в событии
     * @throws ru.practicum.server.exception.NotFoundException если пользователь или событие не найдены
     * @throws ru.practicum.server.exception.ConflictException если пользователь не является инициатором события
     */
    List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId);

     /**
     * Изменяет статус запросов на участие в событии (подтверждение или отклонение).
     *
     * @param userId  ID пользователя-инициатора события
     * @param eventId ID события
     * @param request данные для обновления статусов запросов
     * @return результат обновления с подтвержденными и отклоненными запросами
     * @throws ru.practicum.server.exception.NotFoundException если пользователь или событие не найдены
     * @throws ru.practicum.server.exception.ConflictException если пользователь не является инициатором события
     * @throws ru.practicum.server.exception.ConflictException если лимит участников превышен
     */
    EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdate request);
}
