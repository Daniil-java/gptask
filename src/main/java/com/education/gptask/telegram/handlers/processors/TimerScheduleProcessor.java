package com.education.gptask.telegram.handlers.processors;

import com.education.gptask.entities.timer.Timer;
import com.education.gptask.services.TimerService;
import com.education.gptask.services.UserService;
import com.education.gptask.telegram.enteties.BotState;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class TimerScheduleProcessor {
    private final TimerService timerService;
    private final UserService userService;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public List<BotApiMethod> checkTimesTimeStatus() {
        List<Timer> timers = timerService.getExpiredTimersAndUpdate();
        if (timers == null || timers.isEmpty()) return null;
        List<BotApiMethod> messages = new ArrayList<>();
        for (Timer timer: timers) {
            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(timer.getUserEntity().getChatId());
            editMessageText.setMessageId(timer.getTelegramMessageId());
            editMessageText.setText("Timer has been expired!");
            editMessageText.setReplyMarkup(getInlineMessageButtons());
            messages.add(editMessageText);

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(timer.getUserEntity().getChatId());
            sendMessage.setText("Timer has been expired!");
            sendMessage.setReplyMarkup(getInlineMessageButtonDelete());
            messages.add(sendMessage);
        }
        return messages;
    }

    private InlineKeyboardMarkup getInlineMessageButtons() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton startButton = new InlineKeyboardButton("Старт");
        InlineKeyboardButton pauseButton = new InlineKeyboardButton("Пауза");

        startButton.setCallbackData(BotState.TIMER_START.getCommand());
        pauseButton.setCallbackData(BotState.TIMER_PAUSE.getCommand());

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(startButton);
        row1.add(pauseButton);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(row1);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup getInlineMessageButtonDelete() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton deleteButton = new InlineKeyboardButton("OK");

        deleteButton.setCallbackData(BotState.NOTIFICATION_TIMER_ALERT.getCommand());

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(deleteButton);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(row1);


        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }
}
