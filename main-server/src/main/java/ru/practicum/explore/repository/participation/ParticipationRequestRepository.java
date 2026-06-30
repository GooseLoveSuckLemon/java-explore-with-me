package ru.practicum.explore.repository.participation;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explore.model.participationRequest.ParticipationRequest;
import ru.practicum.explore.model.participationRequest.RequestStatus;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findByRequesterId(Long userId);

    List<ParticipationRequest> findByEventIdAndStatus(Long eventId, RequestStatus status);

    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    List<ParticipationRequest> findByEventId(Long eventId);

    List<ParticipationRequest> findByIdIn(List<Long> requestIds);

    List<ParticipationRequest> findByEventIdAndStatusIn(Long eventId, List<RequestStatus> statuses);
}
