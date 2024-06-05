package com.education.gptask.repositories;

import com.education.gptask.entities.timer.Timer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimerRepository extends JpaRepository<Timer, Long> {

    Optional<List<Timer>> findTimersByUserEntityId(Long userId);

    @EntityGraph(attributePaths = {"tasks"})
    Optional<Timer> findById(Long id);

    @Query("SELECT t FROM Timer t WHERE t.stopTime < :currentTime AND t.status <> 'PENDING'")
    Optional<List<Timer>> findAllExpiredAndNotPending(@Param("currentTime") LocalDateTime currentTime);

    @Modifying
    @Query("UPDATE Timer t SET t.status = 'PENDING' WHERE t.id IN :ids")
    void updateStatusToPending(@Param("ids") List<Long> ids);
}
