package com.education.gptask.telegram.handlers.timer;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.entities.task.Task;
import com.education.gptask.entities.timer.Timer;
import com.education.gptask.entities.timer.TimerStatus;
import com.education.gptask.services.TimerService;
import com.education.gptask.telegram.TelegramBot;
import com.education.gptask.telegram.entities.BotState;
import com.education.gptask.telegram.handlers.MessageHandler;
import com.education.gptask.telegram.utils.builders.BotApiMethodBuilder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@AllArgsConstructor
public class TimerHandler implements MessageHandler {
    private final TimerService timerService;
    private final TelegramBot telegramBot;
    @Override
    public BotApiMethod handle(Message message, UserEntity userEntity) {
        Long chatId = message.getChatId();
        int messageId = message.getMessageId();
        BotState botState = userEntity.getBotState();
        SendMessage replyMessage = new SendMessage(String.valueOf(chatId),
                "Что-то пошло не так ¯\\_(ツ)_/¯");

        Timer timer = timerService.getOrCreateTimerByUserId(userEntity.getId()).get(0);

        if (botState.equals(BotState.TIMER)) {
            replyMessage.setReplyMarkup(getInlineMessageButtons());
            replyMessage.setText(getTimerInfo(timer));
            if (timer.getTelegramMessageId() != 0) {
                DeleteMessage deleteMessage =
                        new DeleteMessage(String.valueOf(chatId), timer.getTelegramMessageId());
                telegramBot.sendMessage(deleteMessage);
            }
            DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), messageId);
            telegramBot.sendMessage(deleteMessage);
            timerService.resetIntervalById(timer.getId());
        }

        if (botState.equals(BotState.TIMER_STATUS)) {
            EditMessageText editMessageText = BotApiMethodBuilder
                    .makeEditMessageText(chatId, timer.getTelegramMessageId(), getTimerInfo(timer));
            switch (timer.getStatus()) {
                case RUNNING: editMessageText.setReplyMarkup(TimerHandler.getInlineMessageStartButtons());
                case PAUSED: editMessageText.setReplyMarkup(getInlineMessagePauseButtons());
                case PENDING: editMessageText.setReplyMarkup(getInlineMessageButtons());
            }
            return editMessageText;
        }

        if (botState.equals(BotState.TIMER_STOP)) {
            timer = timerService.updateTimerStatus(timer.getId(), TimerStatus.PENDING.name());
            timerService.resetIntervalById(timer.getId());
            EditMessageText editMessageText = BotApiMethodBuilder
                    .makeEditMessageText(chatId, messageId, getTimerInfo(timer));
            editMessageText.setReplyMarkup(getInlineMessageButtons());
            return editMessageText;
        }

        if (botState.equals(BotState.TIMER_START)) {
            timer = timerService.updateTimerStatus(timer.getId(), TimerStatus.RUNNING.name());

            EditMessageText editMessageText = BotApiMethodBuilder
                    .makeEditMessageText(chatId, messageId,
                            getTimerInfo(timer));
            editMessageText.setReplyMarkup(getInlineMessageStartButtons());
            return editMessageText;
        }

        if (botState.equals(BotState.TIMER_PAUSE)) {
            timer = timerService.updateTimerStatus(timer.getId(), TimerStatus.PAUSED.name());
            EditMessageText editMessageText = BotApiMethodBuilder
                    .makeEditMessageText(chatId, messageId, getTimerInfo(timer));
            editMessageText.setReplyMarkup(TimerHandler.getInlineMessagePauseButtons());
            return editMessageText;
        }
        timerService.getTimersByUserId(userEntity.getId(), telegramBot.sendReturnedMessage(replyMessage).getMessageId());

        return null;
    }

    public static String getTimerInfo(Timer timer, String firstText) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(firstText);
        for (Task task: timer.getTasks()) {
            stringBuilder.append(task.getId() + "\n"
                    + task.getName() + "\n"
                    + task.getComment() + "\n"
                    + task.getStatus() + "\n\n");
        }
        stringBuilder.append(timer.getStatus());
        return stringBuilder.toString();
    }

    public static String getTimerInfo(Timer timer) {
        StringBuilder stringBuilder = new StringBuilder();
        if (timer.getTasks() != null && !timer.getTasks().isEmpty()) {
            for (Task task: timer.getTasks()) {
                stringBuilder.append(task.getId() + "\n" + task.getName() + "\n" + task.getComment() + "\n\n");
            }
        }
        stringBuilder.append(timer.getStatus());
        return stringBuilder.toString();
    }

    public static InlineKeyboardMarkup getInlineMessageStartButtons() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton tasksListButton = new InlineKeyboardButton("Задачи");
        InlineKeyboardButton pauseButton = new InlineKeyboardButton("Пауза");
        InlineKeyboardButton stopButton = new InlineKeyboardButton("Стоп");

        tasksListButton.setCallbackData(BotState.TIMER_TASKS_LIST.getCommand());
        pauseButton.setCallbackData(BotState.TIMER_PAUSE.getCommand());
        stopButton.setCallbackData(BotState.TIMER_STOP.getCommand());

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(pauseButton);
        row1.add(stopButton);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(Arrays.asList(tasksListButton));
        rowList.add(row1);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup getInlineMessagePauseButtons() {
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

    public static InlineKeyboardMarkup getInlineMessageButtons() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton tasksListButton = new InlineKeyboardButton("Задачи");
        InlineKeyboardButton startButton = new InlineKeyboardButton("Старт");

        tasksListButton.setCallbackData(BotState.TIMER_TASKS_LIST.getCommand());
        startButton.setCallbackData(BotState.TIMER_START.getCommand());

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(startButton);
        row1.add(tasksListButton);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(row1);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    @Override
    public List<BotState> getHandlerListName() {
        return Arrays.asList(
                BotState.TIMER, BotState.TIMER_START, BotState.TIMER_PAUSE, BotState.TIMER_STOP, BotState.TIMER_STATUS);

    }
}
