package ru.practicum.server.controller;

import java.time.LocalDateTime;

/**
 * Базовый абстрактный контроллер, содержащий общие константы и методы валидации.
 * Все контроллеры в проекте должны наследовать этот класс для единообразия.
 *
 * <p>Содержит:
 * <ul>
 *   <li>Константы для форматирования дат</li>
 *   <li>Методы валидации параметров пагинации</li>
 *   <li>Методы валидации диапазона дат</li>
 * </ul>
 *
 * @author Goose
 * @version 1.0
 * @since 2026-06-26
 */
public abstract class BaseController {

    /**
     * Валидация диапазона дат.
     * Проверяет, что начальная дата не позже конечной.
     *
     * @param start начальная дата (может быть null)
     * @param end конечная дата (может быть null)
     * @throws IllegalArgumentException если start позже чем end
     */
    protected void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("Дата начала не может быть позже конечной даты");
        }
    }
}
