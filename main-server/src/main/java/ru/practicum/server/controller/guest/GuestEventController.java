package ru.practicum.server.controller.guest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.server.controller.BaseController;
import ru.practicum.server.dto.event.EventDto;
import ru.practicum.server.dto.event.EventShortDto;
import ru.practicum.server.service.event.EventService;
import ru.practicum.stats.client.StatsClient;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.server.util.Constants.*;


/**
 * Контроллер для обработки запросов к событиям от неавторизованных пользователей (гостей).
 * <p>
 * Предоставляет публичный доступ к просмотру событий и их детальной информации без необходимости аутентификации.
 * Все запросы логируются через сервис статистики для последующего анализа.
 * </p>
 *
 * @see EventService
 * @see StatsClient
 */
@RestController
@RequestMapping(value = {"/events", "/events/"})
@RequiredArgsConstructor
@Slf4j
public class GuestEventController extends BaseController {

    private final EventService eventService;

     /**
     * Получает список событий с возможностью фильтрации и сортировки.
     * <p>
     * Метод обрабатывает GET-запросы к эндпоинту "/events" и возвращает список кратких DTO событий,
     * соответствующих заданным критериям фильтрации. Поддерживается полнотекстовый поиск,
     * фильтрация по категориям, статусу оплаты, временному диапазону, доступности мест,
     * а также сортировка и пагинация результатов.
     * </p>
     * <p>
     * Каждый вызов метода фиксируется в сервисе статистики для отслеживания популярности эндпоинта.
     * </p>
     *
     * @param text          текст для полнотекстового поиска в названии и описании события (может быть null)
     * @param categories    список идентификаторов категорий для фильтрации (может быть null)
     * @param paid          флаг, указывающий, должны ли события быть платными (может быть null)
     * @param rangeStart    начало временного диапазона для фильтрации по дате события (может быть null)
     * @param rangeEnd      конец временного диапазона для фильтрации по дате события (может быть null)
     * @param onlyAvailable флаг, указывающий, возвращать ли только события со свободными местами (по умолчанию false)
     * @param sort          критерий сортировки результатов (например, "EVENT_DATE" или "VIEWS") (может быть null)
     * @param from          начальная позиция для пагинации (по умолчанию 0)
     * @param size          количество записей на странице (по умолчанию 10)
     * @param request       объект HTTP-запроса для получения IP-адреса клиента
     * @return список кратких DTO событий, удовлетворяющих критериям фильтрации
     */
    @GetMapping
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = DEFAULT_FROM) @Min(MIN_FROM) Integer from,
            @RequestParam(defaultValue = DEFAULT_SIZE) @Min(MIN_SIZE) @Max(MAX_SIZE) Integer size,
            HttpServletRequest request) {

        log.info("Запрос гост на получение списка событий - text: {}, categories: {}, paid: {}, " +
                        "rangeStart: {}, rangeEnd: {}, onlyAvailable: {}, sort: {}, from: {}, size: {}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        return eventService.getPublicEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }

    /**
     * Получает подробную информацию о конкретном событии по его идентификатору.
     * <p>
     * Метод обрабатывает GET-запрос к эндпоинту "/events/{id}" и возвращает полную DTO события.
     * При каждом обращении к событию увеличивается счётчик просмотров в возвращаемом DTO.
     * </p>
     * <p>
     * Каждый вызов метода фиксируется в сервисе статистики с указанием конкретного идентификатора события
     * для отслеживания популярности отдельных событий.
     * </p>
     *
     * @param id      идентификатор запрашиваемого события
     * @param request объект HTTP-запроса для получения IP-адреса клиента
     * @return полная DTO запрашиваемого события с обновлённым количеством просмотров
     * @throws ru.practicum.server.exception.NotFoundException если событие с указанным ID не найдено
     */
    @GetMapping("/{id}")
    public EventDto getEvent(@PathVariable Long id, HttpServletRequest request) {

        return eventService.getPublicEvent(id, request);
    }
}
