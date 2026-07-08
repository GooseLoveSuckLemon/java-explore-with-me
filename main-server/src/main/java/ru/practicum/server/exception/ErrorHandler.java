package ru.practicum.server.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Основной обработчик ошибок приложения.
 * Наследует все методы от {@link BaseExceptionHandler}.
 *
 * @author Goose
 * @version 1.0
 * @since 2026-06-26
 */
@RestControllerAdvice
public class ErrorHandler extends BaseExceptionHandler {
}