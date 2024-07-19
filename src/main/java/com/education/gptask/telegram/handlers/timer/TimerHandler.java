package com.education.gptask.telegram.handlers.timer;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.entities.task.Task;
import com.education.gptask.entities.timer.Timer;
import com.education.gptask.entities.timer.TimerIntervalState;
import com.education.gptask.entities.timer.TimerStatus;
import com.education.gptask.services.TaskService;
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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@AllArgsConstructor
public class TimerHandler implements MessageHandler {
    private final TimerService timerService;
    private final TaskService taskService;
    private final TelegramBot telegramBot;
    @Override
    public BotApiMethod handle(Message message, UserEntity userEntity) {
        Long chatId = message.getChatId();
        int messageId = message.getMessageId();
        BotState botState = userEntity.getBotState();
        SendMessage replyMessage = new SendMessage(String.valueOf(chatId),
                "Что-то пошло не так ¯\\_(ツ)_/¯");

        Timer timer = timerService.getOrCreateTimerByUserId(userEntity.getId()).get(0);
        List<Task> taskList = null;
        if (!timer.getTasks().isEmpty()) {
            taskList = taskService.getTasksByTimerId(timer.getId());
        }

        if (botState.equals(BotState.TIMER)) {
            replyMessage.setReplyMarkup(getInlineMessageTimerStatusButtons(timer.getStatus()));
            replyMessage.setText(getTimerInfo(timer, taskList));
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
                    .makeEditMessageText(chatId, timer.getTelegramMessageId(), getTimerInfo(timer, taskList));
            editMessageText.setReplyMarkup(getInlineMessageTimerStatusButtons(timer.getStatus()));
            return editMessageText;
        }

        if (botState.equals(BotState.TIMER_STOP)) {
            timer = timerService.updateTimerStatus(timer.getId(), TimerStatus.PENDING.name());
            timerService.resetIntervalById(timer.getId());
            EditMessageText editMessageText = BotApiMethodBuilder
                    .makeEditMessageText(chatId, messageId, getTimerInfo(timer, taskList));
            editMessageText.setReplyMarkup(getInlineMessageTimerStatusButtons(timer.getStatus()));
            return editMessageText;
        }

        if (botState.equals(BotState.TIMER_START)) {
            timer = timerService.updateTimerStatus(timer.getId(), TimerStatus.RUNNING.name());

            EditMessageText editMessageText = BotApiMethodBuilder
                    .makeEditMessageText(chatId, messageId,
                            getTimerInfo(timer, taskList));
            editMessageText.setReplyMarkup(getInlineMessageTimerStatusButtons(timer.getStatus()));
            return editMessageText;
        }

        if (botState.equals(BotState.TIMER_PAUSE)) {
            timer = timerService.updateTimerStatus(timer.getId(), TimerStatus.PAUSED.name());

            EditMessageText editMessageText = BotApiMethodBuilder
                    .makeEditMessageText(chatId, messageId, getTimerInfo(timer, taskList));
            editMessageText.setReplyMarkup(getInlineMessageTimerStatusButtons(timer.getStatus()));
            return editMessageText;
        }
        timerService.getTimersByUserId(userEntity.getId(), telegramBot.sendReturnedMessage(replyMessage).getMessageId());

        return null;
    }

    public static String getTimerInfo(Timer timer,  String firstText, List<Task> taskList) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(firstText).append("\n");
        return stringBuilder.append(getTimerInfo(timer, taskList)).toString();
    }

    public static String getTimerInfo(Timer timer, List<Task> taskList) {
        StringBuilder stringBuilder = new StringBuilder();
        if (taskList != null && !taskList.isEmpty()) {
            for (Task task : taskList) {
                stringBuilder.append(String.format(
                        "\uD83D\uDE80 [%s] %s\n",
                        task.getPriority(), task.getName()
                ));
                for (Task child : task.getChildTasks()) {
                    stringBuilder.append("      \uD83D\uDCCC ").append(String.format(
                            "[%s] %s\n",
                            child.getPriority(), child.getName()
                    ));
                }
                stringBuilder.append("\n");
            }
        }
        if (!timer.getStatus().equals(TimerStatus.PENDING) && timer.getStopTime() != null) {
            String timeString = timer.getStopTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            stringBuilder.append("⏱ ").append("Время остановки: ").append(timeString);
            stringBuilder.append("\n");
        }

        stringBuilder.append("**Интервал: ").append((timer.getInterval() / 2) + 1).append("\n");
        stringBuilder.append(TimerIntervalState.getTimerState(timer).getState()).append("\n");
        if (timer.getStatus().equals(TimerStatus.PAUSED)) {
            stringBuilder.append("\uD83D\uDCE2 Таймер остановлен\n");
        }

        return stringBuilder.toString();
    }

    public static InlineKeyboardMarkup getInlineMessageTimerStatusButtons(TimerStatus timerStatus) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        InlineKeyboardButton tasksListButton = new InlineKeyboardButton("\uD83D\uDCCB");
        InlineKeyboardButton settingsListButton = new InlineKeyboardButton("⚙");
        InlineKeyboardButton startButton = new InlineKeyboardButton("▶️");
        InlineKeyboardButton stopButton = new InlineKeyboardButton("⏹");
        InlineKeyboardButton pauseButton = new InlineKeyboardButton("⏸");

        startButton.setCallbackData(BotState.TIMER_START.getCommand());
        settingsListButton.setCallbackData(BotState.TIMER_SETTINGS.getCommand());
        tasksListButton.setCallbackData(BotState.TIMER_TASKS_LIST.getCommand());
        pauseButton.setCallbackData(BotState.TIMER_PAUSE.getCommand());
        stopButton.setCallbackData(BotState.TIMER_STOP.getCommand());

        rowList.add(Arrays.asList(settingsListButton, tasksListButton));
        switch (timerStatus) {
            case PENDING:
                rowList.add(Arrays.asList(startButton));
                break;
            case RUNNING:
                rowList.add(Arrays.asList(stopButton, pauseButton));
                break;
            case PAUSED:
                rowList.add(Arrays.asList(stopButton, startButton));
                break;
        }

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    @Override
    public List<BotState> getHandlerListName() {
        return Arrays.asList(
                BotState.TIMER, BotState.TIMER_START, BotState.TIMER_PAUSE, BotState.TIMER_STOP, BotState.TIMER_STATUS);

    }
}
