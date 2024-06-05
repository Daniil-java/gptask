package com.education.gptask.telegram.handlers.timer;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.telegram.enteties.BotState;
import com.education.gptask.telegram.handlers.MessageHandler;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@AllArgsConstructor
public class TimerStopHandler implements MessageHandler {
    private final TimerHandler timerHandler;

    @Override
    public BotApiMethod handle(Message message, UserEntity userEntity) {
        return timerHandler.handle(message, userEntity);
    }

    @Override
    public BotState getHandlerName() {
        return BotState.TIMER_STOP;
    }
}
