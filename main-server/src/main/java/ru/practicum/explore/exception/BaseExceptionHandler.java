package ru.practicum.explore.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Базовый обработчик исключений для всего приложения.
 * Перехватывает все исключения и преобразует их в структурированный JSON ответ.
 *
 * <p>Обрабатывает:
 * <ul>
 *   <li>{@link MethodArgumentNotValidException} - ошибки валидации (400)</li>
 *   <li>{@link NotFoundException} - объект не найден (404)</li>
 *   <li>{@link ConflictException} - конфликт данных (409)</li>
 *   <li>{@link IllegalArgumentException} - неверные параметры (400)</li>
 *   <li>{@link Exception} - внутренние ошибки (500)</li>
 * </ul>
 *
 * @author Goose
 * @version 1.0
 * @since 2026-06-26
 */
@RestControllerAdvice
@Slf4j
public class BaseExceptionHandler {

    /**
     * Форматтер для отображения времени в сообщениях об ошибках
     */
    protected static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Обработчик ошибок валидации.
     *
     * @param e исключение валидации
     * @return структурированный ответ с информацией об ошибке
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(MethodArgumentNotValidException e) {
        log.error("Ошибка валидации: {}", e.getMessage());
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> "Поле: " + fe.getField() + ". Ошибка: " + fe.getDefaultMessage() + ". Значение: " + fe.getRejectedValue())
                .toList();

        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Некорректный запрос.")
                .message("Ошибка валидации")
                .errors(errors)
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    /**
     * Обработчик исключения "Объект не найден".
     *
     * @param e исключение NotFoundException
     * @return структурированный ответ с информацией об ошибке
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException e) {
        log.error("Не найдено: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND.name())
                .reason("Требуемый объект не найден.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    /**
     * Обработчик исключения конфликта данных.
     *
     * @param e исключение ConflictException
     * @return структурированный ответ с информацией об ошибке
     */
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(ConflictException e) {
        log.error("Конфликт: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.CONFLICT.name())
                .reason("Условия для запрошенной операции не соблюдены.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    /**
     * Обработчик исключений неверных аргументов.
     *
     * @param e исключение IllegalArgumentException
     * @return структурированный ответ с информацией об ошибке
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Bad request: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Некорректно сформированный запрос.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    /**
     * Обработчик всех непредвиденных исключений.
     *
     * @param e исключение Exception
     * @return структурированный ответ с информацией об ошибке
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(Exception e) {
        log.error("Internal error: {}", e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .reason("Internal server error.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("Несоответствие типов: {}", e.getMessage());

        if (e.getMessage() != null && e.getMessage().contains("For input string: \"null\"")) {
            return ApiError.builder()
                    .status(HttpStatus.BAD_REQUEST.name())
                    .reason("Invalid parameter format")
                    .message("Значение параметра «null» недопустимо.")
                    .timestamp(LocalDateTime.now().format(FORMATTER))
                    .build();
        }

        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Некорректно сформированный запрос.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingParams(MissingServletRequestParameterException e) {
        log.error("Отсутствует параметр: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Некорректно сформированный запрос")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }
}
