package com.education.gptask.telegram.handlers.task.creation;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.telegram.enteties.BotState;
import com.education.gptask.telegram.handlers.MessageHandler;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@AllArgsConstructor
public class TaskNameCreationHandler implements MessageHandler {
    private final TaskCreationHandler taskCreationHandler;
    @Override
    public BotApiMethod handle(Message message, UserEntity userEntity) {
        return taskCreationHandler.handle(message, userEntity);
    }

    @Override
    public BotState getHandlerName() {
        return BotState.TASK_CREATE_NAME;
    }
}
