package ru.practicum.server.service.compilation;

import ru.practicum.server.dto.compilation.CompilationDto;
import ru.practicum.server.dto.compilation.NewCompilationDto;
import ru.practicum.server.dto.compilation.UpdateCompilationRequest;

import java.util.List;

/**
 * Сервис для управления подборками событий.
 * Предоставляет методы для создания, обновления, удаления и получения подборок.
 *
 * @author Goose
 * @version 1.0
 * @since 2026-07-08
 */
public interface CompilationService {

    /**
     * Создает новую подборку событий.
     *
     * @param dto данные для создания подборки
     * @return созданная подборка в виде DTO
     * @throws ru.practicum.server.exception.NotFoundException если указанные события не найдены
     */
    CompilationDto createCompilation(NewCompilationDto dto);

    /**
     * Удаляет подборку по ID.
     *
     * @param compId ID подборки для удаления
     * @throws ru.practicum.server.exception.NotFoundException если подборка не найдена
     */
    void deleteCompilation(Long compId);

    /**
     * Обновляет существующую подборку.
     *
     * @param compId  ID подборки для обновления
     * @param request данные для обновления подборки
     * @return обновленная подборка в виде DTO
     * @throws ru.practicum.server.exception.NotFoundException если подборка не найдена
     * @throws ru.practicum.server.exception.NotFoundException если указанные события не найдены
     */
    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request);

    /**
     * Получает список подборок с возможностью фильтрации по закрепленным.
     *
     * @param pinned флаг для фильтрации по закрепленным подборкам (может быть null)
     * @param from   начальная позиция для пагинации
     * @param size   количество записей на странице
     * @return список подборок в виде DTO
     */
    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    /**
     * Получает подборку по ID.
     *
     * @param compId ID подборки
     * @return подборка в виде DTO
     * @throws ru.practicum.server.exception.NotFoundException если подборка не найдена
     */
    CompilationDto getCompilation(Long compId);
}