package com.education.gptask.controllers;

import com.education.gptask.dtos.TimerDto;
import com.education.gptask.services.TimerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/timer")
@RequiredArgsConstructor
public class TimerController {
    private final TimerService timerService;

    @GetMapping
    public List<TimerDto> getTimersByUserId(@RequestParam Long userId) {
        return timerService.getTimersByUserId(userId);
    }

    @GetMapping("/{timerId}")
    public TimerDto getTimerById(@PathVariable Long timerId) {
        return timerService.getTimerById(timerId);
    }

    @PostMapping
    public TimerDto createTimer(@RequestBody TimerDto timerDto) {
        return timerService.createTimer(timerDto);
    }

    @PutMapping
    public TimerDto updateTimer(@RequestBody TimerDto timerDto) {
        return timerService.updateTimer(timerDto);
    }

    @PostMapping("/{timerId}/change-status")
    public TimerDto updateTimerStatus(@PathVariable Long timerId, @RequestParam String status) {
        return timerService.updateTimerStatus(timerId, status);
    }

    @DeleteMapping("/{timerId}")
    public void deleteTimerById(@PathVariable Long timerId) {
        timerService.deleteTimerById(timerId);
    }


}
