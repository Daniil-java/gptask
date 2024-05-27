package com.education.gptask.services;

import com.education.gptask.dtos.requests.TimerBindingRequest;
import com.education.gptask.entities.TimerTask;
import com.education.gptask.entities.task.Task;
import com.education.gptask.entities.timer.Timer;
import com.education.gptask.repositories.TimerTaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class TimerTaskService {
    private final TimerTaskRepository timerTaskRepository;

    @Transactional
    public TimerTask bindTaskToTimer(Long timerId, TimerBindingRequest taskReq) {
        return timerTaskRepository.save(new TimerTask()
                .setTimer(new Timer()
                        .setId(timerId))
                .setTask(new Task()
                        .setId(taskReq.getTaskId()))
        );
    }

    @Transactional
    public void unbindTaskFromTimer(Long timerId, TimerBindingRequest taskReq) {
        timerTaskRepository.deleteTimerTaskByTask_IdAndTimer_Id(taskReq.getTaskId(), timerId);
    }

    @Transactional
    public void unbindAllTasksFromTimer(Long timerId) {
        timerTaskRepository.deleteTimerTaskByTimer_Id(timerId);
    }
}
