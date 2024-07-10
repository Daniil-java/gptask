package com.education.gptask.services;

import com.education.gptask.telegram.handlers.processors.TimerScheduleProcessor;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ScheduleService {
    private final TimerScheduleProcessor timerScheduleProcessor;

    @Scheduled(cron = "0 * * * * *")
    private void timerBotScheduleProcess() {
        timerScheduleProcessor.process();
    }
}
