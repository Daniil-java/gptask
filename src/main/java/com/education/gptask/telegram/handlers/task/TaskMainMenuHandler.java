package com.education.gptask.telegram.handlers.task;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.telegram.enteties.BotState;
import com.education.gptask.telegram.handlers.MessageHandler;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;
import java.util.List;

@Component
@AllArgsConstructor
public class TaskMainMenuHandler implements MessageHandler {
    private final TaskHandler taskHandler;

    @Override
    public BotApiMethod handle(Message message, UserEntity userEntity) {
        userEntity.setBotState(BotState.TASK_MAIN_MENU);
        return taskHandler.handle(message, userEntity);
    }

    @Override
    public List<BotState> getHandlerListName() {
        return Arrays.asList(BotState.TASK_MAIN_MENU);
    }
}
