package ru.practicum.server.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Класс, представляющий структуру ответа с информацией об ошибке,
 * возвращаемую клиенту при возникновении исключительных ситуаций.
 * <p>
 * Используется для единообразного форматирования ошибок API,
 * содержит детали ошибки, статус HTTP и временную метку.
 * </p>
 *
 * @author Goose
 * @version 1.0
 * @see org.springframework.http.HttpStatus
 * @see org.springframework.web.bind.annotation.ExceptionHandler
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {

    /**
     * Список ошибок валидации или дополнительных сообщений об ошибках.
     * <p>
     * Может содержать несколько сообщений, например, при множественных
     * ошибках валидации полей запроса.
     * </p>
     */
    private List<String> errors;

    /**
     * Основное сообщение об ошибке.
     * <p>
     * Содержит краткое описание возникшей проблемы.
     * </p>
     */
    private String message;

    /**
     * Причина возникновения ошибки.
     * <p>
     * Более детальное объяснение, почему произошла ошибка.
     * </p>
     */
    private String reason;

    /**
     * HTTP статус ошибки.
     * <p>
     * Содержит имя статуса, например, "NOT_FOUND", "CONFLICT", "BAD_REQUEST".
     * </p>
     */
    private String status;

    /**
     * Временная метка возникновения ошибки.
     */
    private String timestamp;
}