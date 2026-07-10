package ru.practicum.server.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.server.controller.BaseController;
import ru.practicum.server.dto.compilation.CompilationDto;
import ru.practicum.server.dto.compilation.NewCompilationDto;
import ru.practicum.server.dto.compilation.UpdateCompilationRequest;
import ru.practicum.server.service.compilation.CompilationService;

/**
 * Контроллер для управления подборками событий (Admin API).
 *
 * <p>Предоставляет административные методы для работы с подборками событий.
 * Все эндпоинты доступны только пользователям с ролью ADMIN.
 *
 * <p>Эндпоинты:
 * <ul>
 *   <li>POST /admin/compilations - создание новой подборки</li>
 *   <li>DELETE /admin/compilations/{compId} - удаление подборки</li>
 *   <li>PATCH /admin/compilations/{compId} - обновление подборки</li>
 * </ul>
 *
 * <p>Особенности:
 * <ul>
 *   <li>Подборка может не содержать событий</li>
 *   <li>Подборка может быть закреплена на главной (pinned)</li>
 *   <li>Заголовок подборки должен быть уникальным</li>
 * </ul>
 *
 * @author Goose
 * @version 1.0
 * @see CompilationService
 * @see CompilationDto
 * @see NewCompilationDto
 * @see UpdateCompilationRequest
 * @since 2026-06-26
 */
@RestController
@RequestMapping(value = {"/admin/compilations", "/admin/compilations/"})
@RequiredArgsConstructor
@Validated
public class AdminCompilationController extends BaseController {

    private final CompilationService compilationService;

    /**
     * Добавление новой подборки.
     *
     * <p>Создаёт подборку с указанным заголовком и списком событий.
     * Подборка может не содержать событий.
     *
     * @param dto данные новой подборки (заголовок, список событий, закрепление)
     * @return созданная подборка
     * @throws ru.practicum.server.exception.ConflictException если заголовок уже существует
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto saveCompilation(@Valid @RequestBody NewCompilationDto dto) {
        return compilationService.createCompilation(dto);
    }

    /**
     * Удаление подборки.
     *
     * <p>Удаляет подборку по ID.
     *
     * @param compId идентификатор подборки (из пути)
     * @throws ru.practicum.server.exception.NotFoundException если подборка не найдена
     */
    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        compilationService.deleteCompilation(compId);
    }

    /**
     * Обновление подборки.
     *
     * <p>Позволяет изменить заголовок, список событий и статус закрепления.
     * Если поле не указано (null) - изменение не требуется.
     *
     * @param compId идентификатор подборки (из пути)
     * @param request данные для обновления подборки
     * @return обновлённая подборка
     * @throws ru.practicum.server.exception.NotFoundException если подборка не найдена
     * @throws ru.practicum.server.exception.ConflictException если новый заголовок уже существует
     */
    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(@PathVariable Long compId,
                                            @Valid @RequestBody UpdateCompilationRequest request) {
        return compilationService.updateCompilation(compId, request);
    }
}

