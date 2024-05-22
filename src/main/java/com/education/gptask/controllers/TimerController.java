package com.education.gptask.controllers;

import com.education.gptask.dtos.TimerDto;
import com.education.gptask.services.TimerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/timer")
@RequiredArgsConstructor
public class TimerController {
    private final TimerService timerService;

    @GetMapping
    public TimerDto getTimerByUserId(@RequestParam Long userId) {
        return timerService.getTimerByUserId(userId);
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
    public TimerDto updateTimerStatus(@PathVariable Long timerId, @RequestBody TimerDto timerDto) {
        return timerService.updateTimerStatus(timerId, timerDto);
    }

    @DeleteMapping("/{timerId}")
    public void deleteTimerById(@PathVariable Long timerId) {
        timerService.deleteTimerById(timerId);
    }


}
