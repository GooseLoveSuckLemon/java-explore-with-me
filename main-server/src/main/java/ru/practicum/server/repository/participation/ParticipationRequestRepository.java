package ru.practicum.server.repository.participation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.server.model.participation.ParticipationRequest;
import ru.practicum.server.model.participation.ParticipationStatus;

import java.util.List;

@Repository
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    /**
     * Поиск всех запросов пользователя.
     *
     * <p>Возвращает все запросы на участие, созданные указанным пользователем.
     * Используется для отображения истории заявок пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список запросов пользователя, отсортированный по дате создания
     */
    List<ParticipationRequest> findByRequesterId(Long userId);

    /**
     * Поиск запросов на участие в событии с указанным статусом.
     *
     * <p>Используется для получения запросов с определённым статусом
     * при управлении запросами инициатором события.
     *
     * <p>Примеры использования:
     * <ul>
     *   <li>Получение PENDING запросов для подтверждения/отклонения</li>
     *   <li>Получение CONFIRMED запросов для подсчёта участников</li>
     * </ul>
     *
     * @param eventId идентификатор события
     * @param status статус запроса
     * @return список запросов с указанным статусом
     */
    List<ParticipationRequest> findByEventIdAndStatus(Long eventId, ParticipationStatus status);

    /**
     * Проверка существования запроса пользователя на событие.
     *
     * <p>Используется для предотвращения дублирования запросов
     * от одного пользователя на одно событие.
     *
     * @param userId идентификатор пользователя
     * @param eventId идентификатор события
     * @return {@code true}, если запрос уже существует,
     *         {@code false} в противном случае
     */
    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    /**
     * Поиск всех запросов на указанное событие.
     *
     * <p>Возвращает все запросы на участие в событии независимо от статуса.
     * Используется инициатором события для просмотра всех заявок.
     *
     * @param eventId идентификатор события
     * @return список всех запросов на событие
     */
    List<ParticipationRequest> findByEventId(Long eventId);

    /**
     * Поиск запросов по списку идентификаторов.
     *
     * <p>Используется для массового обновления статусов запросов
     * (подтверждение или отклонение нескольких запросов одновременно).
     *
     * @param requestIds список идентификаторов запросов
     * @return список найденных запросов
     */
    List<ParticipationRequest> findByIdIn(List<Long> requestIds);
}
