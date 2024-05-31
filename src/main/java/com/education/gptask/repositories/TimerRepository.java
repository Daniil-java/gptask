package com.education.gptask.repositories;

import com.education.gptask.entities.timer.Timer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimerRepository extends JpaRepository<Timer, Long> {

    Optional<List<Timer>> findTimersByUserEntityId(Long userId);

    @EntityGraph(attributePaths = {"tasks"})
    Optional<Timer> findById(Long id);



}
