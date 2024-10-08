package com.education.gptask.controllers;

import com.education.gptask.dtos.TimerDto;
import com.education.gptask.services.TimerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/timer")
@RequiredArgsConstructor
public class TimerController {
    private final TimerService timerService;

    @GetMapping
    public List<TimerDto> getTimersByUserId(@RequestParam Long userId) {
        return timerService.getTimersDtoByUserId(userId);
    }

    @GetMapping("/{timerId}")
    public TimerDto getTimerById(@PathVariable Long timerId) {
        return timerService.getTimerDtoById(timerId);
    }

    @GetMapping("/auth")
    public List<TimerDto> getTimersByAuthentication(Authentication authentication) {
        return timerService.getTimersDtoByAuthentication(authentication);
    }

    @PostMapping
    public TimerDto createTimer(@Validated @RequestBody TimerDto timerDto) {
        return timerService.createTimer(timerDto);
    }

    @PutMapping
    public TimerDto updateTimer(@Validated @RequestBody TimerDto timerDto) {
        return timerService.updateTimerDto(timerDto);
    }

    @PostMapping("/{timerId}/change-status")
    public TimerDto updateTimerStatus(@PathVariable Long timerId, @RequestParam String status) {
        return timerService.updateTimerDtoStatus(timerId, status);
    }

    @DeleteMapping("/{timerId}")
    public void deleteTimerById(@PathVariable Long timerId) {
        timerService.deleteTimerById(timerId);
    }

    @PostMapping("/{timerId}/tasks/{taskId}")
    public TimerDto bindTaskToTimer(@PathVariable Long timerId, @PathVariable Long taskId) {
        return timerService.bindTaskToTimer(timerId, taskId);
    }

    @DeleteMapping("/{timerId}/tasks/{taskId}")
    public TimerDto unbindTaskFromTimer(@PathVariable Long timerId, @PathVariable Long taskId) {
        return timerService.unbindTaskFromTimer(timerId, taskId);
    }

    @DeleteMapping("/{timerId}/tasks/")
    public TimerDto unbindAllTaskToTimer(@PathVariable Long timerId) {
        return timerService.unbindAllTasksFromTimer(timerId);
    }

    @PostMapping("/save")
    public TimerDto saveTimerFromFront(Authentication authentication, @RequestBody TimerDto timerDto) {
        return timerService.saveTimerFromWeb(timerDto, authentication);
    }
}
