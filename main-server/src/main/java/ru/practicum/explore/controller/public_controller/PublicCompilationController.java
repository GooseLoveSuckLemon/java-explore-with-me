package ru.practicum.explore.controller.public_controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.controller.BaseController;
import ru.practicum.explore.dto.compilation.CompilationDto;
import ru.practicum.explore.service.compilation.CompilationService;

import java.util.List;

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
@RestController
@RequestMapping(value = {"/compilations", "/compilations/"})
@RequiredArgsConstructor
public class PublicCompilationController extends BaseController {

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
                                                @RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
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
     * @throws ru.practicum.explore.exception.NotFoundException если подборка не найдена
     */
    @GetMapping("/{compId}")
    public CompilationDto getCompilation(@PathVariable Long compId) {
        return compilationService.getCompilation(compId);
    }
}