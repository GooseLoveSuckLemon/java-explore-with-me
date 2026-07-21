package ru.practicum.server.repository.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.server.model.rating.EventRating;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с рейтингами событий.
 * <p>
 * Предоставляет методы для доступа к данным рейтингов событий в базе данных.
 * Рейтинги позволяют пользователям оценивать события (лайк/дизлайк).
 * </p>
 *
 * <p>Основные операции:
 * <ul>
 *   <li>Поиск оценки пользователя для события</li>
 *   <li>Подсчёт количества лайков и дизлайков для события</li>
 *   <li>Массовый подсчёт оценок для списка событий</li>
 *   <li>Подсчёт оценок для событий определённых авторов</li>
 *   <li>Проверка существования оценки</li>
 * </ul>
 * </p>
 *
 * @author Goose
 * @version 1.0
 */
@Repository
public interface EventRatingRepository extends JpaRepository<EventRating, Long> {

    /**
     * Находит оценку пользователя для конкретного события.
     * <p>
     * Используется для:
     * <ul>
     *   <li>Проверки, оценивал ли пользователь событие</li>
     *   <li>Получения текущей оценки для обновления</li>
     *   <li>Отображения оценки пользователя в UI</li>
     * </ul>
     * </p>
     *
     * @param eventId идентификатор события
     * @param userId  идентификатор пользователя
     * @return {@link Optional}, содержащий оценку, если она существует
     */
    Optional<EventRating> findByEventIdAndUserId(Long eventId, Long userId);

    /**
     * Проверяет существование оценки пользователя для события.
     * <p>
     * Используется для предотвращения дублирования оценок и валидации
     * перед созданием новой оценки.
     * </p>
     *
     * @param eventId идентификатор события
     * @param userId  идентификатор пользователя
     * @return {@code true}, если оценка существует, иначе {@code false}
     */
    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    /**
     * Подсчитывает количество лайков для указанного события.
     * <p>
     * Используется для формирования статистики рейтинга события.
     * </p>
     *
     * @param eventId идентификатор события
     * @return количество положительных оценок (лайков)
     */
    @Query("SELECT COUNT(r) FROM EventRating r WHERE r.event.id = :eventId AND r.isLike = true")
    long countLikesByEventId(@Param("eventId") Long eventId);

    /**
     * Подсчитывает количество дизлайков для указанного события.
     * <p>
     * Используется для формирования статистики рейтинга события.
     * </p>
     *
     * @param eventId идентификатор события
     * @return количество отрицательных оценок (дизлайков)
     */
    @Query("SELECT COUNT(r) FROM EventRating r WHERE r.event.id = :eventId AND r.isLike = false")
    long countDislikesByEventId(@Param("eventId") Long eventId);

    /**
     * Подсчитывает общее количество оценок для списка событий.
     * <p>
     * Возвращает массив объектов, где каждый элемент содержит:
     * <ul>
     *   <li>[0] - идентификатор события (Long)</li>
     *   <li>[1] - общее количество оценок для этого события (Long)</li>
     * </ul>
     * </p>
     * <p>
     * Используется для массового получения статистики рейтингов
     * при отображении списка событий.
     * </p>
     *
     * @param eventIds список идентификаторов событий
     * @return список массивов объектов с идентификатором события и количеством оценок
     */
    @Query("SELECT r.event.id, COUNT(r) FROM EventRating r WHERE r.event.id IN :eventIds GROUP BY r.event.id")
    List<Object[]> countRatingsByEventIds(@Param("eventIds") List<Long> eventIds);

    /**
     * Подсчитывает количество лайков для списка событий.
     * <p>
     * Возвращает массив объектов, где каждый элемент содержит:
     * <ul>
     *   <li>[0] - идентификатор события (Long)</li>
     *   <li>[1] - количество лайков для этого события (Long)</li>
     * </ul>
     * </p>
     * <p>
     * Используется для массового получения статистики лайков
     * при сортировке событий по рейтингу.
     * </p>
     *
     * @param eventIds список идентификаторов событий
     * @return список массивов объектов с идентификатором события и количеством лайков
     */
    @Query("SELECT r.event.id, COUNT(r) FROM EventRating r WHERE r.event.id IN :eventIds AND r.isLike = true GROUP BY r.event.id")
    List<Object[]> countLikesByEventIds(@Param("eventIds") List<Long> eventIds);

    /**
     * Подсчитывает количество дизлайков для списка событий.
     * <p>
     * Возвращает массив объектов, где каждый элемент содержит:
     * <ul>
     *   <li>[0] - идентификатор события (Long)</li>
     *   <li>[1] - количество дизлайков для этого события (Long)</li>
     * </ul>
     * </p>
     * <p>
     * Используется для массового получения статистики дизлайков
     * при сортировке событий по рейтингу.
     * </p>
     *
     * @param eventIds список идентификаторов событий
     * @return список массивов объектов с идентификатором события и количеством дизлайков
     */
    @Query("SELECT r.event.id, COUNT(r) FROM EventRating r WHERE r.event.id IN :eventIds AND r.isLike = false GROUP BY r.event.id")
    List<Object[]> countDislikesByEventIds(@Param("eventIds") List<Long> eventIds);

    /**
     * Подсчитывает количество лайков для событий, созданных указанными пользователями.
     * <p>
     * Возвращает массив объектов, где каждый элемент содержит:
     * <ul>
     *   <li>[0] - идентификатор пользователя-автора (Long)</li>
     *   <li>[1] - общее количество лайков на всех событиях этого автора (Long)</li>
     * </ul>
     * </p>
     * <p>
     * Используется для формирования рейтинга авторов событий.
     * </p>
     *
     * @param userIds список идентификаторов пользователей-авторов
     * @return список массивов объектов с идентификатором пользователя и количеством лайков
     */
    @Query("SELECT r.event.initiator.id, COUNT(r) FROM EventRating r WHERE r.event.initiator.id IN :userIds AND r.isLike = true GROUP BY r.event.initiator.id")
    List<Object[]> countLikesByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * Подсчитывает количество дизлайков для событий, созданных указанными пользователями.
     * <p>
     * Возвращает массив объектов, где каждый элемент содержит:
     * <ul>
     *   <li>[0] - идентификатор пользователя-автора (Long)</li>
     *   <li>[1] - общее количество дизлайков на всех событиях этого автора (Long)</li>
     * </ul>
     * </p>
     * <p>
     * Используется для формирования рейтинга авторов событий.
     * </p>
     *
     * @param userIds список идентификаторов пользователей-авторов
     * @return список массивов объектов с идентификатором пользователя и количеством дизлайков
     */
    @Query("SELECT r.event.initiator.id, COUNT(r) FROM EventRating r WHERE r.event.initiator.id IN :userIds AND r.isLike = false GROUP BY r.event.initiator.id")
    List<Object[]> countDislikesByUserIds(@Param("userIds") List<Long> userIds);
}