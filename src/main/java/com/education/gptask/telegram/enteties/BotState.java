package com.education.gptask.telegram.enteties;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BotState {
    START("/start"),
    TIMER("/timer"), TIMER_START("/timer_start"),
    TIMER_PAUSE("/timer_pause"), TIMER_STOP("/timer_stop"),
    TIMER_SETTING("/timer_settings"), TIMER_PENDING("/timer_pending"),
    TASK("/task"),
    PROCESSING(""), NOTIFICATION_TIMER_ALERT("/notification_timer_alert");

    private String command;

    public static BotState fromCommand(String command) {
        for (BotState state : BotState.values()) {
            if (state.getCommand().equals(command)) {
                return state;
            }
        }
        return BotState.PROCESSING;
    }
}
