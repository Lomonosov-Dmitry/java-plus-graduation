package ru.practicum.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.event.enums.RequestStatus;
import ru.practicum.model.Request;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    @Query("""
            SELECT r
            FROM Request as r
            WHERE r.eventId.id = :eventId
            AND r.userId.id = :userId
            """)
    Optional<Request> findByUserIdAndEventId(long userId, long eventId);

    @Query("""
            SELECT r
            FROM Request as r
            WHERE r.userId.id = :userId
            """)
    Collection<Request> findAllByUserId(long userId);

    @Query("""
            SELECT r
            FROM Request as r
            WHERE r.eventId.id = :eventId
            """)
    Collection<Request> findAllByEventId(long eventId);

    @Query("""
            SELECT r
            FROM Request as r
            WHERE (r.eventId.id = :eventId)
            AND (r.status = :status)
            AND (r.id IN :requestIds)
            """)
    Collection<Request> findAllRequestsOnEventByIdsAndStatus(long eventId, RequestStatus status, List<Long> requestIds);

    @Query("""
            SELECT r
            FROM Request as r
            WHERE (r.eventId.id = :eventId)
            AND (r.id IN :requestIds)
            """)
    Collection<Request> findAllRequestsOnEventByIds(long eventId, List<Long> requestIds);

}
