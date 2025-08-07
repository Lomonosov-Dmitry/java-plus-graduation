package ru.practicum.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.grpc.stats.event.RecommendedEventProto;
import ru.practicum.model.UserAction;

import java.util.List;

@Repository
public interface UserActionsRepository extends JpaRepository<UserAction, Long> {

    List<UserAction> findAllByEventId(Long eventId);

    @Query(value = """
            select ua.event_id   as event_id,
                   sum(ua.score) as score
            from user_action ua
            where ua.event_id = :eventId
            group by ua.event_id
            
            """, nativeQuery = true)
    List<RecommendedEventProto> findInteractionsCount(Long eventId);
}
