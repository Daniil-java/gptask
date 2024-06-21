package com.education.gptask.telegram.handlers.task.creation;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.telegram.enteties.BotState;
import com.education.gptask.telegram.handlers.MessageHandler;
import com.education.gptask.telegram.handlers.task.list.TaskListHandler;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@AllArgsConstructor
public class SubtaskCreationHandler implements MessageHandler {
    private final TaskListHandler taskListHandler;
    @Override
    public BotApiMethod handle(Message message, UserEntity userEntity) {
        return taskListHandler.handle(message, userEntity);
    }

    @Override
    public BotState getHandlerName() {
        return BotState.TASK_CREATE_SUBTASK;
    }
}
