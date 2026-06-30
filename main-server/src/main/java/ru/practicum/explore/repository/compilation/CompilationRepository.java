package ru.practicum.explore.repository.compilation;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.explore.model.compilation.Compilation;

import java.util.List;

/**
 * Репозиторий для работы с подборками событий.
 *
 * <p>Предоставляет методы для доступа к данным подборок в базе данных.
 * Подборки представляют собой коллекции событий, объединённых по определённому
 * принципу (тематические, популярные, рекомендованные и т.д.).
 *
 * <p>Основные операции:
 * <ul>
 *   <li>Создание и обновление подборок</li>
 *   <li>Поиск подборок с фильтрацией по закреплённости (pinned)</li>
 *   <li>Получение списка событий в подборке</li>
 *   <li>Удаление подборок</li>
 * </ul>
 *
 * <p>Особенности:
 * <ul>
 *   <li>Подборки могут быть закреплены на главной странице (pinned = true)</li>
 *   <li>Одна подборка может содержать множество событий</li>
 *   <li>Одно событие может входить в несколько подборок</li>
 * </ul>
 *
 * @author Goose
 * @version 1.0
 * @since 2026-07-01
 */
@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {


     /**
     * Поиск подборок по признаку закреплённости с пагинацией.
     *
     * <p>Возвращает список подборок, отфильтрованных по полю {@code pinned}.
     *
     * <p>Используется для:
     * <ul>
     *   <li>Отображения закреплённых подборок на главной странице</li>
     *   <li>Показа всех подборок (как закреплённых, так и нет)</li>
     *   <li>Административного управления подборками</li>
     * </ul>
     *
     * @param pinned признак закреплённости подборки ({@code true} - закреплена,
     *               {@code false} - не закреплена, {@code null} - все подборки)
     * @param pageable объект пагинации, содержащий информацию о странице и сортировке
     * @return список подборок, соответствующих критериям
     */
    List<Compilation> findByPinned(Boolean pinned, Pageable pageable);
}
