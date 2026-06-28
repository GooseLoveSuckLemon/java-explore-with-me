package ru.practicum.explore.controller.admin_controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.controller.BaseController;
import ru.practicum.explore.dto.category.CategoryDto;
import ru.practicum.explore.dto.category.NewCategoryDto;
import ru.practicum.explore.service.category.CategoryService;

/**
 * Контроллер для управления категориями (Admin API).
 *
 * <p>Предоставляет административные методы для работы с категориями событий.
 * Все эндпоинты доступны только пользователям с ролью ADMIN.
 *
 * <p>Эндпоинты:
 * <ul>
 *   <li>POST /admin/categories - создание новой категории</li>
 *   <li>PATCH /admin/categories/{catId} - обновление категории</li>
 *   <li>DELETE /admin/categories/{catId} - удаление категории</li>
 * </ul>
 *
 * <p>Ограничения:
 * <ul>
 *   <li>Имя категории должно быть уникальным</li>
 *   <li>Нельзя удалить категорию, если с ней связано хотя бы одно событие</li>
 * </ul>
 *
 * @author Goose
 * @version 1.0
 * @see CategoryService
 * @see CategoryDto
 * @see NewCategoryDto
 * @since 2026-06-26
 */
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Validated
public class AdminCategoryController extends BaseController {

    private final CategoryService categoryService;

    /**
     * Добавление новой категории.
     *
     * <p>Создаёт категорию с указанным именем.
     * Имя категории должно быть уникальным.
     *
     * @param dto данные для создания категории (имя)
     * @return созданная категория с присвоенным ID
     * @throws ru.practicum.explore.exception.ConflictException если категория с таким именем уже существует
     * @throws ru.practicum.explore.exception.NotFoundException если категория не найдена
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@Valid @RequestBody NewCategoryDto dto) {
        return categoryService.createCategory(dto);
    }

    /**
     * Изменение категории.
     *
     * <p>Обновляет название категории по её ID.
     * Новое имя должно быть уникальным.
     *
     * @param catId идентификатор категории (из пути)
     * @param dto данные для обновления категории (новое имя)
     * @return обновлённая категория
     * @throws ru.practicum.explore.exception.NotFoundException если категория не найдена
     * @throws ru.practicum.explore.exception.ConflictException если новое имя уже занято
     */
    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId,
                                      @Valid @RequestBody CategoryDto dto) {
        return categoryService.updateCategory(catId, dto);
    }

    /**
     * Удаление категории.
     *
     * <p>Удаляет категорию по ID.
     * Категория может быть удалена только если с ней не связано ни одного события.
     *
     * @param catId идентификатор категории (из пути)
     * @throws ru.practicum.explore.exception.NotFoundException если категория не найдена
     * @throws ru.practicum.explore.exception.ConflictException если категория не пуста (связана с событиями)
     */
    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        categoryService.deleteCategory(catId);
    }
}