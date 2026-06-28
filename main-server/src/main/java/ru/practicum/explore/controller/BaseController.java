package ru.practicum.explore.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
     * Шаблон для форматирования даты и времени в API
     */
    protected static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * Значение по умолчанию для параметра "from"
     */
    protected static final String DEFAULT_FROM = "0";

    /**
     * Значение по умолчанию для параметра "size" (размер страницы)
     */
    protected static final String DEFAULT_SIZE = "10";

    /**
     * Форматтер для преобразования даты в строку и обратно
     */
    protected static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    /**
     * Валидация параметров.
     *
     * @param from начальный индекс
     * @param size размер страницы
     * @throws IllegalArgumentException если параметры не соответствуют требованиям
     */
    protected void validatePagination(Integer from, Integer size) {
        if (from < 0) {
            throw new IllegalArgumentException("Parameter 'from' must be >= 0");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Parameter 'size' must be >= 1");
        }
    }

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
            throw new IllegalArgumentException("Дата начала должна быть перед датой окончания");
        }
    }
}