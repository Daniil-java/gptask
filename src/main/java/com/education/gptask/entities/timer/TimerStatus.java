package com.education.gptask.entities.timer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TimerStatus {
    PENDING,
    RUNNING,
    PAUSED,
    COMPLETED,
    STOPPED
}
