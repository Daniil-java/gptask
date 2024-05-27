package com.education.gptask.repositories;

import com.education.gptask.entities.TimerTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimerTaskRepository extends JpaRepository<TimerTask, Long> {
    void deleteTimerTaskByTask_IdAndTimer_Id(Long taskId, Long timerId);

    void deleteTimerTaskByTimer_Id(Long timerId);
}
