package ru.practicum.dal;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.event.enums.EventState;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("""
            SELECT e
            FROM Event as e
            WHERE (:userIds IS NULL OR e.initiator.id in :userIds)
            AND (:states IS NULL OR e.state in :states)
            AND (:categoryIds IS NULL OR e.category.id in :categoryIds)
            AND (CAST(:start AS DATE) IS NULL OR e.eventDate >= :start)
            AND (CAST(:end AS DATE) IS NULL OR e.eventDate <= :end)
            """)
    List<Event> findAllByFilter(List<Long> userIds,
                                List<String> states,
                                List<Long> categoryIds,
                                LocalDateTime start,
                                LocalDateTime end,
                                Pageable pageable);

    @Query("""
            SELECT e
            FROM Event as e
            WHERE (:text IS NULL OR (e.annotation ilike %:text% OR e.description ilike %:text%))
            AND (:categoryIds IS NULL OR e.category.id in :categoryIds)
            AND (:paid IS NULL OR e.paid = :paid)
            AND (e.eventDate >= :start)
            AND (e.state = :state)
            AND (:onlyAvailable = false OR e.participantLimit = 0 OR e.confirmedRequests <= e.participantLimit)
            AND (CAST(:end AS DATE) IS NULL OR e.eventDate <= :end)
            """)
    List<Event> findAllByFilterPublic(String text,
                                      List<Long> categoryIds,
                                      Boolean paid,
                                      LocalDateTime start,
                                      LocalDateTime end,
                                      Boolean onlyAvailable,
                                      EventState state,
                                      Pageable pageable);

    @Query("""
            SELECT e
            FROM Event as e
            WHERE e.id = :eventId
            AND e.initiator.id = :userId
            """)
    Optional<Event> findByIdAndUserId(long eventId, long userId);

    @Query("""
            SELECT e
            FROM Event as e
            WHERE e.initiator.id = :userId
            """)
    List<Event> findByUserId(long userId, Pageable pageable);

    @Query("""
            SELECT e
            FROM Event as e
            WHERE e.id = :eventId
            AND e.state = :state
            """)
    Optional<Event> findByIdAndStatus(long eventId, EventState state);
}
