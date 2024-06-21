package com.education.gptask.telegram.handlers.timer.controls.tasklist;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.entities.task.Task;
import com.education.gptask.entities.timer.Timer;
import com.education.gptask.telegram.enteties.BotState;
import com.education.gptask.telegram.handlers.MessageHandler;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@AllArgsConstructor
public class TimerTasksListUnbindHandler  implements MessageHandler {
    private final TimerTasksListHandler timerTasksListHandler;
    @Override
    public BotApiMethod handle(Message message, UserEntity userEntity) {
        return timerTasksListHandler.handle(message, userEntity);
    }

    public static InlineKeyboardMarkup getInlineMessageButtons(Timer timer) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        for (Task task: timer.getTasks()) {
            InlineKeyboardButton button = new InlineKeyboardButton(task.getId().toString());
            button.setCallbackData("/delete" + task.getId());
            rowList.add(Arrays.asList(button));
        }

        InlineKeyboardButton button = new InlineKeyboardButton("Назад");
        button.setCallbackData(BotState.TIMER_TASKS_LIST.getCommand());
        rowList.add(Arrays.asList(button));

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    @Override
    public BotState getHandlerName() {
        return BotState.TIMER_TASKS_LIST_DELETE;
    }
}
