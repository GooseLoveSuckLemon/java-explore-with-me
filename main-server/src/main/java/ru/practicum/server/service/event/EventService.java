package ru.practicum.server.service.event;

import ru.practicum.server.dto.event.EventDto;
import ru.practicum.server.dto.event.EventShortDto;
import ru.practicum.server.dto.event.NewEventDto;
import ru.practicum.server.dto.event.update.UpdateEventAdminRequest;
import ru.practicum.server.dto.event.update.UpdateEventUserRequest;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для управления событиями.
 * Предоставляет методы для создания, обновления, получения и удаления событий
 * с учетом ролей пользователей (администратор, пользователь, гость).
 *
 * @author Goose
 * @version 1.0
 * @since 2026-07-08
 */
public interface EventService {

    /**
     * Создает новое событие от имени пользователя.
     *
     * @param userId       ID пользователя-инициатора
     * @param newEventDto  данные для создания события
     * @return созданное событие в виде DTO
     * @throws ru.practicum.server.exception.NotFoundException если пользователь или категория не найдены
     * @throws IllegalArgumentException если дата события меньше чем через 2 часа от текущего момента
     */
    EventDto createEvent(Long userId, NewEventDto newEventDto);

    /**
     * Получает список событий, созданных пользователем.
     *
     * @param userId ID пользователя-инициатора
     * @param from   начальная позиция для пагинации
     * @param size   количество записей на странице
     * @return список событий пользователя в виде DTO
     * @throws ru.practicum.server.exception.NotFoundException если пользователь не найден
     */
    List<EventDto> getUserEvents(Long userId, Integer from, Integer size);

    /**
     * Получает конкретное событие пользователя по ID.
     *
     * @param userId  ID пользователя-инициатора
     * @param eventId ID события
     * @return событие в виде DTO
     * @throws ru.practicum.server.exception.NotFoundException если событие не найдено или не принадлежит пользователю
     */
    EventDto getUserEvent(Long userId, Long eventId);

    /**
     * Обновляет событие пользователя.
     *
     * @param userId  ID пользователя-инициатора
     * @param eventId ID события для обновления
     * @param request данные для обновления события
     * @return обновленное событие в виде DTO
     * @throws ru.practicum.server.exception.NotFoundException если событие не найдено
     * @throws ru.practicum.server.exception.ConflictException если событие уже опубликовано
     * @throws IllegalArgumentException если дата события меньше чем через 2 часа от текущего момента
     */
    EventDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request);

    /**
     * Получает список событий для администратора с фильтрацией.
     *
     * @param users      список ID пользователей для фильтрации (может быть null)
     * @param states     список статусов событий для фильтрации (может быть null)
     * @param categories список ID категорий для фильтрации (может быть null)
     * @param rangeStart начало временного диапазона для фильтрации (может быть null)
     * @param rangeEnd   конец временного диапазона для фильтрации (может быть null)
     * @param from       начальная позиция для пагинации
     * @param size       количество записей на странице
     * @return список событий в виде DTO
     * @throws IllegalArgumentException если rangeStart позже rangeEnd
     */
    List<EventDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                    LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    /**
     * Обновляет событие администратором (публикация или отклонение).
     *
     * @param eventId ID события для обновления
     * @param request данные для обновления события
     * @return обновленное событие в виде DTO
     * @throws ru.practicum.server.exception.NotFoundException если событие не найдено
     * @throws ru.practicum.server.exception.ConflictException если событие нельзя опубликовать/отклонить
     * @throws IllegalArgumentException если дата события меньше чем через 1 час от текущего момента
     */
    EventDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request);

    /**
     * Получает список публичных событий с фильтрацией и сортировкой.
     * Доступно для неавторизованных пользователей (гостей).
     *
     * @param text          текст для полнотекстового поиска (может быть null)
     * @param categories    список ID категорий для фильтрации (может быть null)
     * @param paid          флаг оплаты события (может быть null)
     * @param rangeStart    начало временного диапазона (может быть null)
     * @param rangeEnd      конец временного диапазона (может быть null)
     * @param onlyAvailable флаг возврата только событий со свободными местами
     * @param sort          критерий сортировки ("EVENT_DATE" или "VIEWS") (может быть null)
     * @param from          начальная позиция для пагинации
     * @param size          количество записей на странице
     * @return список кратких DTO событий
     * @throws IllegalArgumentException если rangeStart позже rangeEnd
     */
    List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        Boolean onlyAvailable, String sort, Integer from,
                                        Integer size, String ip);

    /**
     * Получает публичное событие по ID с увеличением счетчика просмотров.
     * Доступно для неавторизованных пользователей (гостей).
     *
     * @param eventId ID события
     * @return событие в виде DTO с обновленным количеством просмотров
     * @throws ru.practicum.server.exception.NotFoundException если событие не найдено или не опубликовано
     */
    EventDto getPublicEvent(Long eventId, String ip);

    /**
     * Удаляет событие по ID.
     *
     * @param eventId ID события для удаления
     * @throws ru.practicum.server.exception.NotFoundException если событие не найдено
     */
    void deleteEvent(Long eventId);
}
