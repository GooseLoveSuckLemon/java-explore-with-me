package ru.practicum.server.repository.event;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.server.model.event.Event;
import ru.practicum.server.model.event.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Репозиторий для работы с сущностью {@link Event}.
 * Предоставляет методы для выполнения операций с событиями в базе данных.
 *
 * <p>Расширяет {@link JpaRepository} для базовых CRUD операций и
 * {@link JpaSpecificationExecutor} для динамических запросов с использованием спецификаций.
 *
 * @author Goose
 * @version 1.0
 * @since 2026-07-08
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

     /**
     * Находит все события, созданные указанным пользователем, с пагинацией.
     *
     * <p>Метод используется для получения списка событий конкретного инициатора
     *
     * @param userId   идентификатор пользователя-инициатора событий
     * @param pageable объект пагинации, содержащий информацию о странице и сортировке
     * @return список событий, созданных пользователем
     */
    List<Event> findByInitiatorId(Long userId, Pageable pageable);

     /**
     * Находит событие по идентификатору инициатора и идентификатору события.
     *
     * <p>Использует JPQL запрос для поиска события, принадлежащего конкретному пользователю.
     * Позволяет проверить, что событие действительно создано указанным инициатором.
     *
     * @param userId  идентификатор пользователя-инициатора
     * @param eventId идентификатор события
     */
    @Query("SELECT e FROM Event e WHERE e.initiator.id = :userId AND e.id = :eventId")
    Event findByInitiatorIdAndId(@Param("userId") Long userId, @Param("eventId") Long eventId);

     /**
     * Находит событие по идентификатору события и идентификатору инициатора.
     *
     * <p>Возвращает {@link Optional} для безопасной работы с возможным отсутствием
     * результата.
     *
     * @param eventId идентификатор события
     * @param userId  идентификатор пользователя-инициатора
     */
    @Query("SELECT e FROM Event e WHERE e.id = :eventId AND e.initiator.id = :userId")
    Optional<Event> findByIdAndInitiatorId(@Param("eventId") Long eventId, @Param("userId") Long userId);

     /**
     * Находит опубликованные события по заданным критериям фильтрации с пагинацией.
     *
     * <p>Выполняет нативный SQL запрос для поиска событий со статусом 'PUBLISHED'*
     *
     * @param categories  список категорий для фильтрации
     * @param rangeStart  начало даты проведения события
     * @param rangeEnd    конец даты проведения события
     * @param pageable    объект пагинации для постраничного вывода результатов
     */
    @Query(value = "SELECT * FROM events e WHERE " +
            "e.state = 'PUBLISHED' " +
            "AND (CAST(:text AS TEXT) IS NULL OR " +
            "LOWER(e.annotation) LIKE LOWER(CONCAT('%', CAST(:text AS TEXT), '%')) OR " +
            "LOWER(e.description) LIKE LOWER(CONCAT('%', CAST(:text AS TEXT), '%'))) " +
            "AND (CAST(:categories AS TEXT) IS NULL OR e.category_id IN (:categories)) " +
            "AND (CAST(:paid AS TEXT) IS NULL OR e.paid = :paid) " +
            "AND e.event_date BETWEEN :rangeStart AND :rangeEnd " +
            "ORDER BY e.event_date",
            nativeQuery = true)
    List<Event> findPublishedEvents(@Param("text") String text,
                                    @Param("categories") List<Long> categories,
                                    @Param("paid") Boolean paid,
                                    @Param("rangeStart") LocalDateTime rangeStart,
                                    @Param("rangeEnd") LocalDateTime rangeEnd,
                                    Pageable pageable);

     /**
     * Находит все события с указанным статусом.
     *
     * <p>Позволяет получить список событий по их текущему состоянию
     *
     * @param state статус события, по которому выполняется поиск
     * @see EventState
     */
    List<Event> findByState(EventState state);

     /**
     * Находит все события, принадлежащие указанной категории.
     *
     * <p>Используется для получения списка событий в конкретной категории,
     *
     * @param categoryId идентификатор категории
     * @return список событий, принадлежащих указанной категории
     */
    List<Event> findByCategoryId(Long categoryId);

    @Query("SELECT e.initiator.id, COUNT(e) FROM Event e WHERE e.initiator.id IN :userIds GROUP BY e.initiator.id")
    List<Object[]> countEventsByInitiatorIdsGrouped(@Param("userIds") List<Long> userIds);

    default Map<Long, Long> countEventsByInitiatorIds(List<Long> userIds) {
        List<Object[]> results = countEventsByInitiatorIdsGrouped(userIds);
        return results.stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));
    }

    long countByInitiatorId(Long userId);
}
