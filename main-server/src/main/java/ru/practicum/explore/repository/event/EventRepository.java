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

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

    List<Event> findByInitiatorId(Long userId, Pageable pageable);

    List<Event> findByCategoryId(Long categoryId);

    List<Event> findByState(EventState state);

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