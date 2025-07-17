package ru.practicum.dal;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("""
            SELECT u
            FROM User as u
            WHERE (:ids IS NULL OR u.id in :ids)
            """)
    List<User> findAllByFilter(List<Long> ids, Pageable pageable);
}
