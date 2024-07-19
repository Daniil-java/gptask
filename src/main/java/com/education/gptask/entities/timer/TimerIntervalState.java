package com.education.gptask.entities.timer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TimerIntervalState {
    WORK_INTERVAL("Рабочее время"),
    SHORT_BREAK_INTERVAL("Перерыв"),
    LONG_BREAK_INTERVAL("Большой перерыв"),
    NOT_LAUNCH("Таймер не запущен");

    private String state;

    public static TimerIntervalState getTimerState(Timer timer) {
        if (timer.getStatus().equals(TimerStatus.PENDING)) {
            return NOT_LAUNCH;
        }
        if (timer.getLongBreakInterval() != 0
                && timer.getInterval() % 2 != 0
                && timer.getInterval() / 2 % timer.getLongBreakInterval() != 0) {
            return LONG_BREAK_INTERVAL;
        } else if (timer.getLongBreakInterval() != 0 && timer.getInterval() % 2 != 0 ) {
            return SHORT_BREAK_INTERVAL;
        } else {
            return WORK_INTERVAL;
        }
    }
}
