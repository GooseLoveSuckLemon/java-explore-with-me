package ru.practicum.server.service;

import ru.practicum.server.exception.ConflictException;
import ru.practicum.server.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Базовый абстрактный сервис, содержащий общие методы для работы с сущностями.
 * Все сервисы в проекте должны наследовать этот класс.
 *
 * <p>Содержит:
 * <ul>
 *   <li>Метод для поиска сущности с выбрасыванием исключения</li>
 *   <li>Методы для валидации даты события</li>
 *   <li>Общие константы</li>
 * </ul>
 *
 * @author Goose
 * @version 1.0
 * @since 2026-06-26
 */
public abstract class BaseService {

    /**
     * Минимальное количество часов до начала события
     */
    protected static final int MIN_HOURS_FOR_EVENT = 2;

    /**
     * Поиск сущности по Optional с выбрасыванием исключения.
     * Упрощает обработку случаев, когда сущность не найдена.
     *
     * @param optional Optional с сущностью
     * @param entityName название сущности для сообщения об ошибке
     * @param id идентификатор сущности
     * @param <T> тип сущности
     * @return найденная сущность
     * @throws NotFoundException если сущность не найдена
     */
    protected <T> T findOrThrow(Optional<T> optional, String entityName, Long id) {
        return optional.orElseThrow(() ->
                new NotFoundException(entityName + " с id " + id + " не найдена")
        );
    }

    /**
     * Валидация даты события при создании.
     * Проверяет, что событие создаётся не раньше чем через 2 часа.
     *
     * @param eventDate дата события
     * @throws ConflictException если дата меньше чем через 2 часа
     */
    protected void validateEventDate(LocalDateTime eventDate) {
        if (eventDate != null && eventDate.isBefore(LocalDateTime.now().plusHours(MIN_HOURS_FOR_EVENT))) {
            throw new ConflictException(
                    "Дата события должна быть как минимум на " + MIN_HOURS_FOR_EVENT + " часов позже"
            );
        }
    }

    /**
     * Валидация даты события при обновлении.
     * Проверяет, что дата события (если изменяется) не раньше чем через 2 часа.
     *
     * @param eventDate новая дата события (может быть null)
     * @throws ConflictException если дата меньше чем через 2 часа
     */
    protected void validateEventUpdate(LocalDateTime eventDate) {
        if (eventDate != null && eventDate.isBefore(LocalDateTime.now().plusHours(MIN_HOURS_FOR_EVENT))) {
            throw new ConflictException(
                    "Дата события должна быть как минимум на  " + MIN_HOURS_FOR_EVENT + " часов позже"
            );
        }
    }
}