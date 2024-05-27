package com.education.gptask.controllers;

import com.education.gptask.dtos.requests.TimerBindingRequest;
import com.education.gptask.services.TimerTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/timertasks")
@RequiredArgsConstructor
public class TimerTaskController {
    private final TimerTaskService timerTaskService;

    @PostMapping("/{timerId}/bind-task")
    public void bindTaskToTimer(@PathVariable Long timerId, @RequestBody TimerBindingRequest taskReq) {
        timerTaskService.bindTaskToTimer(timerId, taskReq);
    }

    @DeleteMapping("/{timerId}/unbind-task")
    public void unbindTaskFromTimer(@PathVariable Long timerId, @RequestBody TimerBindingRequest taskReq) {
        timerTaskService.unbindTaskFromTimer(timerId, taskReq);
    }

    @DeleteMapping("/{timerId}/unbind-all-task")
    public void unbindAllTaskToTimer(@PathVariable Long timerId) {
        timerTaskService.unbindAllTasksFromTimer(timerId);
    }
}
