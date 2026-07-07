package ru.practicum.server.controller.guest;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.server.controller.BaseController;
import ru.practicum.server.dto.compilation.CompilationDto;
import ru.practicum.server.service.compilation.CompilationService;

import java.util.List;

import static ru.practicum.server.util.Constants.*;

/**
 * Публичный контроллер для работы с подборками событий.
 *
 * <p>Предоставляет методы для просмотра подборок без аутентификации.
 * Все эндпоинты доступны любому пользователю (включая неавторизованных).
 *
 * <p>Эндпоинты:
 * <ul>
 *   <li>GET /compilations - получение списка подборок</li>
 *   <li>GET /compilations/{compId} - получение подборки по ID</li>
 * </ul>
 *
 * <p>Особенности:
 * <ul>
 *   <li>Только чтение (GET)</li>
 *   <li>Поддерживается фильтрация по статусу закрепления (pinned)</li>
 *   <li>Поддерживается пагинация</li>
 *   <li>Возвращает только существующие подборки</li>
 * </ul>
 *
 * @author Goose
 * @version 1.0
 * @see CompilationService
 * @see CompilationDto
 * @since 2026-06-26
 */
@Slf4j
@RestController
@RequestMapping(value = {"/compilations", "/compilations/"})
@RequiredArgsConstructor
public class GuestCompilationController extends BaseController {

    private final CompilationService compilationService;

    /**
     * Получение подборок событий с фильтрацией.
     *
     * <p>Возвращает список подборок. Можно отфильтровать по статусу закрепления.
     * Поддерживает пагинацию.
     *
     * @param pinned фильтр по закреплению (true - только закреплённые, false - только незакреплённые, null - все)
     * @param from начальный индекс (по умолчанию 0)
     * @param size размер страницы (по умолчанию 10)
     * @return список подборок
     */
    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(defaultValue = DEFAULT_FROM) @Min(MIN_FROM) Integer from,
                                                @RequestParam(defaultValue = DEFAULT_SIZE) @Min(MIN_SIZE) @Max(MAX_SIZE) Integer size) {
        log.info("Запрос гостя на получение подборки - pinned: {}, from: {}, size: {}", pinned, from, size);
        return compilationService.getCompilations(pinned, from, size);
    }

    /**
     * Получение подборки по ID.
     *
     * <p>Возвращает полную информацию о подборке.
     * Если подборка не найдена - возвращает ошибку 404.
     *
     * @param compId идентификатор подборки (из пути)
     * @return подборка с событиями
     * @throws ru.practicum.server.exception.NotFoundException если подборка не найдена
     */
    @GetMapping("/{compId}")
    public CompilationDto getCompilation(@PathVariable Long compId) {
        return compilationService.getCompilation(compId);
    }
}