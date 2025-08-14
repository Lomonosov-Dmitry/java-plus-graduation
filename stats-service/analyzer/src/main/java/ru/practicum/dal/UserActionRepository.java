package ru.practicum.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.UserAction;

import java.util.List;

@Repository
public interface UserActionRepository extends JpaRepository<UserAction, Long> {

    UserAction findByEventIdAndUserId(Long eventId, Long userId);

    boolean existsUserActionByEventIdAndUserId(Long eventId, Long userId);

    @Query(value = """
            select user_id, weight FROM user_actions where event_id = :eventId""", nativeQuery = true)
    List<RecommendedEventI> getInteractions(Long eventId);
}
