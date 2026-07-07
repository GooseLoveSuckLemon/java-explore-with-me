package ru.practicum.server.service.category;

import ru.practicum.server.dto.category.CategoryDto;
import ru.practicum.server.dto.category.NewCategoryDto;

import java.util.List;

/**
 * Сервис для управления категориями событий.
 * Предоставляет методы для создания, обновления, удаления и получения категорий.
 *
 * @author Goose
 * @version 1.0
 * @since 2026-07-08
 */
public interface CategoryService {

     /**
     * Создает новую категорию.
     *
     * @param dto данные для создания категории
     * @return созданная категория в виде DTO
     * @throws ru.practicum.server.exception.ConflictException если категория с таким именем уже существует
     */
    CategoryDto createCategory(NewCategoryDto dto);

     /**
     * Обновляет существующую категорию.
     *
     * @param catId ID категории для обновления
     * @param dto   данные для обновления категории
     * @return обновленная категория в виде DTO
     * @throws ru.practicum.server.exception.NotFoundException если категория не найдена
     * @throws ru.practicum.server.exception.ConflictException если категория с таким именем уже существует
     */
    CategoryDto updateCategory(Long catId, CategoryDto dto);

     /**
     * Удаляет категорию по ID.
     *
     * @param catId ID категории для удаления
     * @throws ru.practicum.server.exception.NotFoundException если категория не найдена
     * @throws ru.practicum.server.exception.ConflictException если категория используется в событиях
     */
    void deleteCategory(Long catId);

     /**
     * Получает список категорий с пагинацией.
     *
     * @param from начальная позиция для пагинации
     * @param size количество записей на странице
     * @return список категорий в виде DTO
     */
    List<CategoryDto> getCategories(Integer from, Integer size);

     /**
     * Получает категорию по ID.
     *
     * @param catId ID категории
     * @return категория в виде DTO
     * @throws ru.practicum.server.exception.NotFoundException если категория не найдена
     */
    CategoryDto getCategory(Long catId);
}

