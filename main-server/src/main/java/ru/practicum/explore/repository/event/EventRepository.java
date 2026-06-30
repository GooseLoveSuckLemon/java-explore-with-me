package ru.practicum.explore.repository.event;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.explore.model.event.Event;
import ru.practicum.explore.model.event.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с событиями.
 *
 * <p>Предоставляет методы для доступа к данным событий в базе данных.
 * Является основным репозиторием для управления событиями в системе.
 *
 * <p>Основные операции:
 * <ul>
 *   <li>Создание и обновление событий</li>
 *   <li>Поиск событий по различным критериям (административный и публичный)</li>
 *   <li>Поиск событий пользователя</li>
 *   <li>Поиск событий по категории</li>
 *   <li>Поиск событий по статусу</li>
 * </ul>
 *
 * <p>Особенности:
 * <ul>
 *   <li>Поддерживает сложные поисковые запросы с фильтрацией</li>
 *   <li>Использует параметризованные запросы для безопасности</li>
 *   <li>Поддерживает пагинацию для больших объёмов данных</li>
 * </ul>
 *
 * @author Goose
 * @version 1.0
 * @since 2026-07-01
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

     /**
     * Поиск события по ID и ID инициатора.
     *
     * <p>Используется для проверки, что событие принадлежит конкретному пользователю.
     * Необходим для методов, где пользователь может управлять только своими событиями.
     *
     * @param eventId идентификатор события
     * @param initiatorId идентификатор пользователя-инициатора
     * @return {@link Optional} с событием, если оно найдено и принадлежит пользователю,
     *         пустой Optional в противном случае
     */
    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

     /**
     * Поиск всех событий пользователя с пагинацией.
     *
     * <p>Возвращает список событий, созданных указанным пользователем.
     * События сортируются в порядке убывания даты создания.
     *
     * @param userId идентификатор пользователя
     * @param pageable объект пагинации
     * @return список событий пользователя
     */
    List<Event> findByInitiatorId(Long userId, Pageable pageable);

     /**
     * Поиск всех событий в указанной категории.
     *
     * <p>Используется при удалении категории для проверки,
     * есть ли связанные события.
     *
     * @param categoryId идентификатор категории
     * @return список событий в категории
     */
    List<Event> findByCategoryId(Long categoryId);

     /**
     * Поиск всех событий с указанным статусом.
     *
     * <p>Используется для получения событий в определённом состоянии,
     * например, для публикации ожидающих событий или для статистики.
     *
     * @param state статус события
     * @return список событий с указанным статусом
     */
    List<Event> findByState(EventState state);

     /**
     * Поиск событий администратором с фильтрацией.
     *
     * <p>Предоставляет расширенный поиск событий для административных целей.
     * Все параметры фильтрации являются опциональными.
     *
     * <p>Параметры фильтрации:
     * <ul>
     *   <li><b>users</b> - список ID пользователей-инициаторов</li>
     *   <li><b>states</b> - список статусов событий (PENDING, PUBLISHED, CANCELED)</li>
     *   <li><b>categories</b> - список ID категорий</li>
     *   <li><b>rangeStart</b> - начало диапазона дат событий</li>
     *   <li><b>rangeEnd</b> - конец диапазона дат событий</li>
     * </ul>
     *
     * <p>Особенности:
     * <ul>
     *   <li>Если параметр не указан (null), он игнорируется в запросе</li>
     *   <li>Для дат: если указан только rangeStart, ищутся события после этой даты</li>
     *   <li>Если указан только rangeEnd, ищутся события до этой даты</li>
     *   <li>Результаты сортируются по умолчанию по ID (зависит от Pageable)</li>
     * </ul>
     *
     * @param users список ID пользователей (опционально)
     * @param states список статусов событий (опционально)
     * @param categories список ID категорий (опционально)
     * @param rangeStart начало диапазона дат (опционально)
     * @param rangeEnd конец диапазона дат (опционально)
     * @param pageable объект пагинации и сортировки
     * @return список событий, соответствующих фильтрам
     */
    @Query("SELECT e FROM Event e\n" +
            "            WHERE (:users IS NULL OR e.initiator.id IN :users)\n" +
            "              AND (:states IS NULL OR e.state IN :states)\n" +
            "              AND (:categories IS NULL OR e.category.id IN :categories)\n" +
            "              AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart)\n" +
            "              AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)")
    List<Event> searchAdmin(List<Long> users,
                            List<String> states,
                            List<Long> categories,
                            LocalDateTime rangeStart,
                            LocalDateTime rangeEnd,
                            Pageable pageable);

     /**
     * Публичный поиск событий с фильтрацией.
     *
     * <p>Предоставляет поиск событий для публичных пользователей.
     * Возвращает только опубликованные события (PUBLISHED).
     *
     * <p>Параметры фильтрации:
     * <ul>
     *   <li><b>text</b> - текст для поиска в аннотации и описании
     *       (регистронезависимый)</li>
     *   <li><b>categories</b> - список ID категорий</li>
     *   <li><b>paid</b> - фильтр по платности (true/false)</li>
     *   <li><b>rangeStart</b> - начало диапазона дат событий</li>
     *   <li><b>rangeEnd</b> - конец диапазона дат событий</li>
     * </ul>
     *
     * <p>Особенности поиска:
     * <ul>
     *   <li>Поиск по тексту выполняется в полях annotation и description</li>
     *   <li>Регистр не учитывается (LOWER)</li>
     *   <li>Все параметры опциональны</li>
     *   <li>Возвращаются только события со статусом PUBLISHED</li>
     *   <li>Если rangeStart и rangeEnd не указаны, используются текущие дата и время</li>
     * </ul>
     *
     * @param text текст для поиска (опционально)
     * @param categories список ID категорий (опционально)
     * @param paid признак платности (опционально)
     * @param rangeStart начало диапазона дат (опционально)
     * @param rangeEnd конец диапазона дат (опционально)
     * @param pageable объект пагинации и сортировки
     * @return список опубликованных событий, соответствующих фильтрам
     */
    @Query("SELECT e FROM Event e\n" +
            "            WHERE e.state = 'PUBLISHED'\n" +
            "              AND (:text IS NULL OR\n" +
            "                   LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) OR\n" +
            "                   LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')))\n" +
            "              AND (:categories IS NULL OR e.category.id IN :categories)\n" +
            "              AND (:paid IS NULL OR e.paid = :paid)\n" +
            "              AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart)\n" +
            "              AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)")
    List<Event> searchPublic(String text,
                             List<Long> categories,
                             Boolean paid,
                             LocalDateTime rangeStart,
                             LocalDateTime rangeEnd,
                             Pageable pageable);
}