package com.education.gptask.repositories;

import com.education.gptask.entities.timer.Timer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TimerRepository extends JpaRepository<Timer, Long> {

    Optional<Timer> findTimerByUserId(Long userId);

}
