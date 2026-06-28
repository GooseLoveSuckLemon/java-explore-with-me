package ru.practicum.explore.controller.public_controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.controller.BaseController;
import ru.practicum.explore.dto.event.EventFullDto;
import ru.practicum.explore.dto.event.EventShortDto;
import ru.practicum.explore.service.event.EventService;
import ru.practicum.explore.service.stats.StatsIntegrationService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Публичный контроллер для работы с событиями.
 *
 * <p>Предоставляет методы для просмотра событий без аутентификации.
 * Все эндпоинты доступны любому пользователю (включая неавторизованных).
 *
 * <p>Эндпоинты:
 * <ul>
 *   <li>GET /events - получение событий с фильтрацией</li>
 *   <li>GET /events/{id} - получение события по ID</li>
 * </ul>
 *
 * <p>Особенности:
 * <ul>
 *   <li>Только опубликованные события</li>
 *   <li>Текстовый поиск без учёта регистра</li>
 *   <li>Сортировка по дате или просмотрам</li>
 *   <li>Фильтрация по категориям, платности, доступности</li>
 *   <li>Автоматическая отправка статистики просмотров</li>
 *   <li>Информация о событии включает количество просмотров и подтверждённых заявок</li>
 * </ul>
 *
 * @author Goose
 * @version 1.0
 * @see EventService
 * @see StatsIntegrationService
 * @see EventShortDto
 * @see EventFullDto
 * @since 2026-06-26
 */
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class PublicEventController extends BaseController {

    private final EventService eventService;

    private final StatsIntegrationService statsService;

    /**
     * Получение событий с возможностью фильтрации.
     *
     * <p>Возвращает только опубликованные события.
     * Если диапазон дат не указан - возвращает события, которые произойдут позже текущего момента.
     *
     * <p>Поддерживаемые фильтры:
     * <ul>
     *   <li>text - текстовый поиск в аннотации и описании</li>
     *   <li>categories - фильтр по категориям</li>
     *   <li>paid - фильтр по платности</li>
     *   <li>rangeStart/rangeEnd - диапазон дат</li>
     *   <li>onlyAvailable - только события с доступными местами</li>
     *   <li>sort - сортировка (EVENT_DATE или VIEWS)</li>
     * </ul>
     *
     * @param text текст для поиска (опционально)
     * @param categories список ID категорий (опционально)
     * @param paid фильтр по платности (опционально)
     * @param rangeStart начало диапазона дат (опционально)
     * @param rangeEnd конец диапазона дат (опционально)
     * @param onlyAvailable только доступные события (по умолчанию false)
     * @param sort вариант сортировки (EVENT_DATE или VIEWS)
     * @param from начальный индекс (по умолчанию 0)
     * @param size размер страницы (по умолчанию 10)
     * @return список событий с краткой информацией
     */
    @GetMapping
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("Getting events with filters: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}",
                text, categories, paid, rangeStart, rangeEnd);
        return eventService.getPublicEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }

    /**
     * Получение подробной информации об опубликованном событии.
     *
     * <p>Возвращает полную информацию о событии.
     * Автоматически отправляет запрос в сервис статистики.
     *
     * <p>Информация включает:
     * <ul>
     *   <li>Все поля события</li>
     *   <li>Количество просмотров</li>
     *   <li>Количество подтверждённых заявок</li>
     * </ul>
     *
     * @param id идентификатор события (из пути)
     * @return полная информация о событии
     * @throws ru.practicum.explore.exception.NotFoundException если событие не найдено или не опубликовано
     */
    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable Long id) {
        log.info("Getting event with id: {}", id);
        return eventService.getPublicEvent(id);
    }
}