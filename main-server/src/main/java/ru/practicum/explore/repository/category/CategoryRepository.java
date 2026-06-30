package ru.practicum.explore.repository.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.explore.model.category.Category;

/**
 * Репозиторий для работы с категориями событий.
 *
 * <p>Предоставляет методы для доступа к данным категорий в базе данных.
 * Расширяет {@link JpaRepository}, что обеспечивает базовый CRUD функционал.
 *
 * <p>Категории используются для классификации событий и не зависят
 * от конкретных событий или пользователей.
 *
 * <p>Основные операции:
 * <ul>
 *   <li>Создание и обновление категорий</li>
 *   <li>Поиск категорий по ID</li>
 *   <li>Проверка существования категории по имени</li>
 *   <li>Удаление категорий (при отсутствии связанных событий)</li>
 * </ul>
 *
 * @author Goose
 * @version 1.0
 * @since 2026-07-01
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

     /**
     * Проверяет существование категории с указанным именем.
     *
     * <p>Используется для валидации при создании или обновлении категории,
     * чтобы предотвратить дублирование названий.
     *
     * <p>Поиск выполняется без учёта регистра (зависит от настроек базы данных).
     *
     * @param name имя категории для проверки
     * @return {@code true}, если категория с таким именем уже существует,
     *         {@code false} в противном случае
     */
    boolean existsByName(String name);
}