package ru.practicum.server.util;

import java.time.format.DateTimeFormatter;

/**
 * Класс для хранения общих констант приложения.
 * Используется во всех слоях приложения.
 *
 * @author Goose
 * @version 1.0
 * @since 2026-07-08
 */
public class Constants {

    /**
     * Шаблон для форматирования даты и времени в API
     */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * Форматтер для преобразования даты в строку и обратно
     */
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    /**
     * Значения пагинации по умолчанию
     */
    public static final String DEFAULT_FROM = "0";
    public static final String DEFAULT_SIZE = "10";

    /**
     * Ограничения для пагинации
     */
    public static final int MAX_SIZE = 100;
    public static final int MIN_FROM = 0;
    public static final int MIN_SIZE = 1;

    /**
     * Название приложения для статистики
     */
    public static final String APP_NAME = "main-server";
}