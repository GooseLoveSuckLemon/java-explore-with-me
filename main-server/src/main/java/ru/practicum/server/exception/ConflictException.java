package ru.practicum.server.exception;

/**
 * Исключение, выбрасываемое при возникновении конфликта данных.
 * <p>
 * Используется для ситуаций, когда операция не может быть выполнена
 * из-за нарушения ограничений целостности данных или бизнес-правил,
 * например, при попытке создания дублирующей записи.
 * </p>
 * <p>
 * Обычно маппится на HTTP статус {@code 409 CONFLICT}.
 * </p>
 *
 * @author Goose
 * @version 1.0
 * @see org.springframework.http.HttpStatus#CONFLICT
 */
public class ConflictException extends RuntimeException {

    /**
     * Конструктор исключения с сообщением об ошибке.
     *
     * @param message сообщение, описывающее причину конфликта
     */
    public ConflictException(String message) {
        super(message);
    }
}
