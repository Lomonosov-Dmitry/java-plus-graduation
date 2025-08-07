package ru.practicum.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.EventSimitarity;

import java.util.List;

@Repository
public interface EventSimilarityRepository extends JpaRepository<EventSimitarity, Long> {
    @Query(value = """
            with user_events as (select ua.event_id from user_action ua where ua.user_id = :userId)
            select * from event_similarity es
            where es.event_a_id in user_events and es.event_b_id not in user_events
                                                group by es.event_b_id
                                                order by es.score desc
                                                limit :limit
            """, nativeQuery = true)
    List<EventSimitarity> findRecommendationsForUser(Long userId, Integer limit);

    @Query(value = """
            with user_events as (select ua.event_id from user_action ua where ua.user_id = :userId)
            select * from event_similarity es
            where es.event_a_id = :eventId and es.event_b_id not in user_events
                                                group by es.event_b_id
                                                order by es.score desc
                                                limit :limit
            """, nativeQuery = true)
    List<EventSimitarity> findSimilarityEvents(Long eventId, Long userId, Integer limit);
}
