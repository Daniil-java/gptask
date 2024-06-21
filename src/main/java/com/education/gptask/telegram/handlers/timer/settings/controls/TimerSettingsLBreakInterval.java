package com.education.gptask.telegram.handlers.timer.settings.controls;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.telegram.enteties.BotState;
import com.education.gptask.telegram.handlers.MessageHandler;
import com.education.gptask.telegram.handlers.timer.settings.TimerSettingsHandler;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@AllArgsConstructor
public class TimerSettingsLBreakInterval implements MessageHandler {
    private final TimerSettingsHandler timerSettingsHandler;
    @Override
    public BotApiMethod handle(Message message, UserEntity userEntity) {
        return timerSettingsHandler.handle(message, userEntity);
    }

    @Override
    public BotState getHandlerName() {
        return BotState.TIMER_SETTINGS_LBREAK_INTERVAL;
    }
}
