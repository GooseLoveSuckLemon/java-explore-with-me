package ru.practicum.explore.repository.participation;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explore.model.participationRequest.ParticipationRequest;
import ru.practicum.explore.model.participationRequest.RequestStatus;

import java.util.List;

/**
 * Репозиторий для работы с запросами на участие в событиях.
 *
 * <p>Предоставляет методы для доступа к данным запросов на участие в базе данных.
 * Управляет процессом подачи заявок на участие в событиях.
 *
 * <p>Основные операции:
 * <ul>
 *   <li>Создание и отмена запросов</li>
 *   <li>Поиск запросов пользователя</li>
 *   <li>Поиск запросов по событию и статусу</li>
 *   <li>Проверка существования запроса</li>
 *   <li>Массовое обновление статусов запросов</li>
 * </ul>
 *
 * <p>Статусы запросов:
 * <ul>
 *   <li><b>PENDING</b> - ожидает подтверждения</li>
 *   <li><b>CONFIRMED</b> - подтверждён</li>
 *   <li><b>REJECTED</b> - отклонён</li>
 *   <li><b>CANCELED</b> - отменён пользователем</li>
 * </ul>
 *
 * @author Goose
 * @version 1.0
 * @since 2026-07-01
 */
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
    List<ParticipationRequest> findByEventIdAndStatus(Long eventId, RequestStatus status);

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

     /**
     * Поиск запросов на событие с указанными статусами.
     *
     * <p>Используется для получения запросов, которые находятся
     * в одном из нескольких статусов.
     *
     * <p>Примеры использования:
     * <ul>
     *   <li>Получение PENDING и CONFIRMED запросов для проверки лимитов</li>
     *   <li>Получение всех неотменённых запросов</li>
     * </ul>
     *
     * @param eventId идентификатор события
     * @param statuses список статусов запросов
     * @return список запросов с указанными статусами
     */
    List<ParticipationRequest> findByEventIdAndStatusIn(Long eventId, List<RequestStatus> statuses);
}
