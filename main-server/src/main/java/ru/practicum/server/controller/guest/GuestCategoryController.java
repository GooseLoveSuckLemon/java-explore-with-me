package ru.practicum.server.controller.guest;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.server.controller.BaseController;
import ru.practicum.server.dto.category.CategoryDto;
import ru.practicum.server.service.category.CategoryService;

import java.util.List;

import static ru.practicum.server.util.Constants.*;

/**
 * Публичный контроллер для работы с категориями.
 *
 * <p>Предоставляет методы для просмотра категорий без аутентификации.
 * Все эндпоинты доступны любому пользователю (включая неавторизованных).
 *
 * <p>Эндпоинты:
 * <ul>
 *   <li>GET /categories - получение списка категорий</li>
 *   <li>GET /categories/{catId} - получение категории по ID</li>
 * </ul>
 *
 * <p>Особенности:
 * <ul>
 *   <li>Только чтение (GET)</li>
 *   <li>Поддерживается пагинация</li>
 *   <li>Возвращает только существующие категории</li>
 *   <li>Если категория не найдена - возвращает 404</li>
 * </ul>
 *
 * @author Goose
 * @version 1.0
 * @see CategoryService
 * @see CategoryDto
 * @since 2026-06-26
 */
@Slf4j
@RestController
@RequestMapping(value = {"/categories", "/categories/"})
@RequiredArgsConstructor
public class GuestCategoryController extends BaseController {

    private final CategoryService categoryService;

    /**
     * Получение списка категорий с пагинацией.
     *
     * <p>Возвращает все категории с поддержкой пагинации.
     * Если категории не найдены - возвращает пустой список.
     *
     * @param from начальный индекс (по умолчанию 0)
     * @param size размер страницы (по умолчанию 10)
     * @return список категорий
     */
    @GetMapping
    public List<CategoryDto> getCategories(@RequestParam(defaultValue = DEFAULT_FROM) @Min(MIN_FROM) Integer from,
                                           @RequestParam(defaultValue = DEFAULT_SIZE) @Min(MIN_SIZE) @Max(MAX_SIZE) Integer size) {
        log.info("Запрос гостя на получение категории: - from: {}, size: {}", from, size);
        return categoryService.getCategories(from, size);
    }

    /**
     * Получение категории по ID.
     *
     * <p>Возвращает категорию по указанному идентификатору.
     * Если категория не найдена - возвращает ошибку 404.
     *
     * @param catId идентификатор категории (из пути)
     * @return категория
     * @throws ru.practicum.server.exception.NotFoundException если категория не найдена
     */
    @GetMapping("/{catId}")
    public CategoryDto getCategory(@PathVariable Long catId) {
        log.info("Запрос гостя на получение категории с ID: {}", catId);
        return categoryService.getCategory(catId);
    }
}