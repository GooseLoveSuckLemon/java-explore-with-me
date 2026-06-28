package ru.practicum.explore.repository.event;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.explore.model.event.Event;
import ru.practicum.explore.model.event.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByInitiatorId(Long userId, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.initiator.id = :userId AND e.id = :eventId")
    Event findByInitiatorIdAndId(@Param("userId") Long userId, @Param("eventId") Long eventId);

    @Query("SELECT e FROM Event e WHERE e.initiator.id = :userId AND e.id = :eventId")
    Optional<Event> findByIdAndInitiatorId(@Param("userId") Long userId, @Param("eventId") Long eventId);

    @Query(value = "SELECT * FROM events e WHERE " +
            "(CAST(:text AS TEXT) IS NULL OR " +
            "e.annotation ILIKE CONCAT('%', CAST(:text AS TEXT), '%') OR " +
            "e.description ILIKE CONCAT('%', CAST(:text AS TEXT), '%')) " +
            "AND e.state = 'PUBLISHED' " +
            "AND (CAST(:categories AS TEXT) IS NULL OR e.category_id IN (:categories)) " +
            "AND (CAST(:paid AS TEXT) IS NULL OR e.paid = :paid) " +
            "AND e.event_date > :now " +
            "ORDER BY e.event_date",
            nativeQuery = true)
    List<Event> findPublishedEvents(@Param("text") String text,
                                    @Param("categories") List<Long> categories,
                                    @Param("paid") Boolean paid,
                                    @Param("now") LocalDateTime now,
                                    Pageable pageable);

    List<Event> findByState(EventState state);
}