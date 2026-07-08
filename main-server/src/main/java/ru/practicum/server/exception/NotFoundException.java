package ru.practicum.server.exception;

/**
 * Исключение, выбрасываемое при попытке доступа к несуществующему ресурсу.
 * <p>
 * Используется для ситуаций, когда запрашиваемый объект не найден в системе,
 * например, при обращении к несуществующему ID или при поиске по отсутствующему значению.
 * </p>
 * <p>
 * Обычно маппится на HTTP статус {@code 404 NOT FOUND}.
 * </p>
 *
 * @author Goose
 * @version 1.0
 * @see org.springframework.http.HttpStatus#NOT_FOUND
 */
public class NotFoundException extends RuntimeException {

    /**
     * Конструктор исключения с сообщением об ошибке.
     *
     * @param message сообщение, описывающее, какой ресурс не найден
     */
    public NotFoundException(String message) {
        super(message);
    }
}
