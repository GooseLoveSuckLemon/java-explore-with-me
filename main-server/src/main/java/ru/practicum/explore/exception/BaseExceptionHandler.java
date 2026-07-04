package ru.practicum.explore.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Базовый обработчик исключений для всего приложения.
 *
 * <p>Предоставляет централизованную обработку исключений, возникающих
 * в процессе работы приложения. Все исключения преобразуются в
 * стандартизированный формат {@link ApiError} с соответствующими
 * HTTP-статусами.
 *
 * <p>Обрабатываемые типы исключений:
 * <ul>
 *   <li>{@link MethodArgumentNotValidException} - ошибки валидации
 *       аргументов методов (Bean Validation)</li>
 *   <li>{@link ConstraintViolationException} - ошибки валидации
 *       параметров запросов и пути</li>
 *   <li>{@link NotFoundException} - запрашиваемый объект не найден</li>
 *   <li>{@link ConflictException} - конфликт при выполнении операции</li>
 *   <li>{@link IllegalArgumentException} - некорректные аргументы</li>
 *   <li>{@link MethodArgumentTypeMismatchException} - несоответствие
 *       типов параметров</li>
 *   <li>{@link Exception} - все остальные непредвиденные ошибки</li>
 * </ul>
 *
 * <p>Формат ответа {@link ApiError}:
 * <ul>
 *   <li><b>status</b> - HTTP статус в виде строки (например, "BAD_REQUEST")</li>
 *   <li><b>reason</b> - краткое описание причины ошибки</li>
 *   <li><b>message</b> - детальное сообщение об ошибке</li>
 *   <li><b>errors</b> - список дополнительных ошибок (для валидации)</li>
 *   <li><b>timestamp</b> - время возникновения ошибки в формате "yyyy-MM-dd HH:mm:ss"</li>
 * </ul>
 *
 * <p>Особенности:
 * <ul>
 *   <li>Все ошибки логируются с соответствующим уровнем логирования</li>
 *   <li>Для ошибок валидации возвращается список всех нарушений</li>
 *   <li>Для необработанных исключений возвращается статус 500 Internal Server Error</li>
 *   <li>Специальная обработка для значения "null" в параметрах запроса</li>
 * </ul>
 *
 * @author Goose
 * @version 1.0
 * @since 2026-07-01
 */
@RestControllerAdvice
@Slf4j
public abstract class BaseExceptionHandler {

     /**
     * Форматтер для преобразования времени в строку.
     * Используется для единообразного форматирования timestamp
     * в ответах с ошибками.
     */
    protected static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

     /**
     * Обработчик ошибок валидации аргументов метода.
     *
     * <p>Срабатывает при ошибках валидации аннотаций {@link jakarta.validation.Valid}
     * в теле запроса (например, @NotBlank, @Size и т.д.).
     *
     * <p>Формирует ответ со списком всех ошибок валидации с указанием:
     * <ul>
     *   <li>Названия поля</li>
     *   <li>Сообщения об ошибке</li>
     *   <li>Отклонённого значения</li>
     * </ul>
     *
     * @param e исключение валидации
     * @return объект {@link ApiError} с описанием ошибок
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
     * Обработчик ошибок валидации параметров запроса.
     *
     * <p>Срабатывает при ошибках валидации параметров пути (@PathVariable),
     * параметров запроса (@RequestParam) и других параметров, аннотированных
     * валидационными аннотациями.
     *
     * <p>Возвращает список всех нарушений с указанием пути к параметру
     * и сообщением об ошибке.
     *
     * @param e исключение нарушения ограничений
     * @return объект {@link ApiError} с описанием ошибок
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolationException(ConstraintViolationException e) {
        log.error("Ошибка валидации: {}", e.getMessage());
        List<String> errors = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
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
     * <p>Срабатывает при попытке получить доступ к несуществующему объекту
     * (например, событие, категория, пользователь и т.д.).
     *
     * <p>Возвращает статус 404 Not Found с описанием того, какой объект
     * не был найден.
     *
     * @param e исключение "не найдено"
     * @return объект {@link ApiError} с описанием ошибки
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
     * <p>Срабатывает при нарушении бизнес-правил или ограничений
     * целостности данных:
     * <ul>
     *   <li>Попытка создать дубликат</li>
     *   <li>Нарушение условий для выполнения операции</li>
     *   <li>Несоответствие статусов</li>
     *   <li>Превышение лимитов</li>
     *   <li>Изменение данных, находящихся в недопустимом состоянии</li>
     * </ul>
     *
     * <p>Возвращает статус 409 Conflict с детальным описанием причины.
     *
     * @param e исключение конфликта
     * @return объект {@link ApiError} с описанием ошибки
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
     * Обработчик исключений недопустимых аргументов.
     *
     * <p>Срабатывает при передаче недопустимых значений в методы,
     * например:
     * <ul>
     *   <li>Обязательный параметр равен null</li>
     *   <li>Некорректное значение параметра</li>
     *   <li>Неверный формат данных</li>
     * </ul>
     *
     * <p>Возвращает статус 400 Bad Request с описанием проблемы.
     *
     * @param e исключение недопустимого аргумента
     * @return объект {@link ApiError} с описанием ошибки
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
     * <p>Является финальным обработчиком для всех исключений,
     * которые не были обработаны другими методами.
     *
     * <p>Возвращает статус 500 Internal Server Error.
     * В сообщении содержится информация об ошибке для целей
     * отладки (не рекомендуется для production).
     *
     * <p><b>Важно:</b> Полный стектрейс логируется для последующего
     * анализа, но не передаётся клиенту.
     *
     * @param e непредвиденное исключение
     * @return объект {@link ApiError} с описанием ошибки
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

     /**
     * Обработчик ошибок несоответствия типов аргументов.
     *
     * <p>Срабатывает, когда переданный параметр не может быть
     * преобразован в ожидаемый тип, например:
     * <ul>
     *   <li>Попытка передать строку в параметр типа Long</li>
     *   <li>Передача некорректной даты</li>
     *   <li>Передача значения "null" в параметр, который не может
     *       быть преобразован из строки</li>
     * </ul>
     *
     * <p>Особенности:
     * <ul>
     *   <li>Специальная обработка случая, когда передано значение "null"</li>
     *   <li>Возвращает понятное пользователю сообщение об ошибке</li>
     * </ul>
     *
     * @param e исключение несоответствия типов
     * @return объект {@link ApiError} с описанием ошибки
     */
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

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiError handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.error("Method not supported: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.METHOD_NOT_ALLOWED.name())
                .reason("Метод не поддерживается")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }
}