package com.education.gptask.telegram.handlers.timer.controls;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.telegram.enteties.BotState;
import com.education.gptask.telegram.handlers.MessageHandler;
import com.education.gptask.telegram.handlers.timer.TimerHandler;
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
public class TimerPauseHandler implements MessageHandler {
    private final TimerHandler timerHandler;

    @Override
    public BotApiMethod handle(Message message, UserEntity userEntity) {
        return timerHandler.handle(message, userEntity);
    }

    public static InlineKeyboardMarkup getInlineMessageButtons() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton tasksListButton = new InlineKeyboardButton("Задачи");
        InlineKeyboardButton startButton = new InlineKeyboardButton("Старт");
        InlineKeyboardButton stopButton = new InlineKeyboardButton("Стоп");

        tasksListButton.setCallbackData(BotState.TIMER_TASKS_LIST.getCommand());
        startButton.setCallbackData(BotState.TIMER_START.getCommand());
        stopButton.setCallbackData(BotState.TIMER_STOP.getCommand());

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(startButton);
        row1.add(stopButton);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(Arrays.asList(tasksListButton));
        rowList.add(row1);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    @Override
    public BotState getHandlerName() {
        return BotState.TIMER_PAUSE;
    }
}
