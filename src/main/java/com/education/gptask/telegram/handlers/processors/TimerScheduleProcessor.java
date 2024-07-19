package com.education.gptask.telegram.handlers.processors;

import com.education.gptask.entities.timer.Timer;
import com.education.gptask.services.TimerService;
import com.education.gptask.telegram.TelegramBot;
import com.education.gptask.telegram.entities.BotState;
import com.education.gptask.telegram.handlers.timer.TimerHandler;
import com.education.gptask.telegram.utils.builders.BotApiMethodBuilder;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
@Slf4j
public class TimerScheduleProcessor {
    private final TimerService timerService;
    private final TelegramBot telegramBot;

    public void process() {
        log.info("Schedule timer checker is starting!");
        List<BotApiMethod> messages = checkTimersTimeStatus();
        if (!CollectionUtils.isEmpty(messages)) {
            log.info("Expired timer list size: {}", messages.size());
            for (BotApiMethod message: messages) {
                try {
                    Thread.sleep(1000);
                    telegramBot.sendMessage(message);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            log.info("Expired timer list is empty");
        }
    }

    @Transactional
    private List<BotApiMethod> checkTimersTimeStatus() {
        List<Timer> timers = timerService.getExpiredTimersAndUpdate();
        if (timers == null || timers.isEmpty()) return null;
        List<BotApiMethod> messages = new ArrayList<>();
        for (Timer timer: timers) {
            log.info(timer.getId() + " --- " + timer.getStatus());
            EditMessageText editMessageText = BotApiMethodBuilder
                    .makeEditMessageText(
                            timer.getUserEntity().getChatId(),
                            timer.getTelegramMessageId(),
                            timer.getStatus().name()
                            );
            editMessageText.setReplyMarkup(TimerHandler.getInlineMessageTimerStatusButtons(timer.getStatus()));
            messages.add(editMessageText);

            SendMessage sendMessage = new SendMessage(
                    String.valueOf(timer.getUserEntity().getChatId()),
                    "Timer has been expired!"
            );
            sendMessage.setReplyMarkup(getInlineMessageButtonDelete());
            messages.add(sendMessage);
        }
        return messages;
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
