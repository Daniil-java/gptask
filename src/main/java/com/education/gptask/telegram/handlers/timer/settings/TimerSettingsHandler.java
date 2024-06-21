package com.education.gptask.telegram.handlers.timer.settings;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.entities.timer.Timer;
import com.education.gptask.services.TimerService;
import com.education.gptask.services.UserService;
import com.education.gptask.telegram.TelegramBot;
import com.education.gptask.telegram.enteties.BotState;
import com.education.gptask.telegram.handlers.MessageHandler;
import com.education.gptask.telegram.utils.builders.BotApiMethodBuilder;
import com.education.gptask.telegram.utils.keyboards.InlineKeyboardBuilder;
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
public class TimerSettingsHandler implements MessageHandler {
    private final UserService userService;
    private final TelegramBot telegramBot;
    private final TimerService timerService;

    private final String SETTINGS_MSG = "Choose message option";

    @Override
    public BotApiMethod handle(Message message, UserEntity userEntity) {
        Long chatId = message.getChatId();
        int messageId = message.getMessageId();
        String userAnswer = message.getText();
        BotState botState = userEntity.getBotState();
        SendMessage replyMessage = new SendMessage(String.valueOf(chatId),
                "Что-то пошло не так ¯\\_(ツ)_/¯");


        Timer timer = timerService.getTimersByUserId(userEntity.getId()).get(0);
        int responseMessageId = 0;
        if (userEntity.getLastUpdatedTimerSettingsMessageId() != null) {
            responseMessageId = Math.toIntExact(userEntity.getLastUpdatedTimerSettingsMessageId());
        }

        EditMessageText editMessageText = BotApiMethodBuilder
                .makeEditMessageText(chatId,
                        responseMessageId,
                        "Enter value"
                );
        if (botState.equals(BotState.TIMER_SETTINGS)) {
            if (userAnswer.equals("CLOSE")) {
                DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId),
                        Math.toIntExact(userEntity.getLastUpdatedTimerSettingsMessageId()));
                return deleteMessage;
            }
            SendMessage sendMessage = new SendMessage(String.valueOf(chatId), SETTINGS_MSG);
            sendMessage.setReplyMarkup(getInlineMessageButtons());
            DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), messageId);
            telegramBot.sendMessage(deleteMessage);
            Message returnedMessage = telegramBot.sendReturnedMessage(sendMessage);

            userEntity.setLastUpdatedTimerSettingsMessageId(Long.valueOf(returnedMessage.getMessageId()));
            userService.updateUserEntity(userEntity);
            return null;
        }

        if (botState.equals(BotState.TIMER_SETTINGS_AUTOSTART_BREAK)) {
            if (userAnswer.equals("true") || userAnswer.equals("false")) {
                timer.setAutostartBreak(Boolean.valueOf(userAnswer));
            } else {
                editMessageText.setText("Choose value");
                editMessageText.setReplyMarkup(InlineKeyboardBuilder.getTrueOrFalseK());
                return editMessageText;
            }
        }

        if (botState.equals(BotState.TIMER_SETTINGS_AUTOSTART_WORK)) {
            if (userAnswer.equals("true") || userAnswer.equals("false")) {
                timer.setAutostartWork(Boolean.valueOf(userAnswer));
            } else {
                editMessageText.setText("Choose value");
                editMessageText.setReplyMarkup(InlineKeyboardBuilder.getTrueOrFalseK());
                return editMessageText;
            }
        }

        if (botState.equals(BotState.TIMER_SETTINGS_LBREAK)) {
            if (!userAnswer.isEmpty() && userAnswer.matches("[0-9]+")) {
                timer.setLongBreakDuration(Integer.parseInt(userAnswer));
            } else return editMessageText;
        }

        if (botState.equals(BotState.TIMER_SETTINGS_LBREAK_INTERVAL)) {
            if (!userAnswer.isEmpty() && userAnswer.matches("[0-9]+")) {
                timer.setLongBreakInterval(Integer.parseInt(userAnswer));
            } else {
                return editMessageText;
            }
        }

        if (botState.equals(BotState.TIMER_SETTINGS_WORK)) {
            if (!userAnswer.isEmpty() && userAnswer.matches("[0-9]+")) {
                timer.setWorkDuration(Integer.parseInt(userAnswer));
            } else return editMessageText;
        }

        if (botState.equals(BotState.TIMER_SETTINGS_SBREAK)) {
            if (!userAnswer.isEmpty() && userAnswer.matches("[0-9]+")) {
                timer.setShortBreakDuration(Integer.parseInt(userAnswer));
            } else return editMessageText;
        }
        if (!botState.equals(BotState.TIMER_SETTINGS)) {
            timerService.updateTimer(timer);
            userEntity.setBotState(BotState.TIMER_SETTINGS);
            userService.updateUserEntity(userEntity);
            DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), messageId);
            editMessageText.setText(SETTINGS_MSG);
            editMessageText.setReplyMarkup(getInlineMessageButtons());
            telegramBot.sendMessage(deleteMessage);
            return editMessageText;
        }

        return replyMessage;
    }

    private InlineKeyboardMarkup getInlineMessageButtons() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton workDuration = new InlineKeyboardButton("workDuration");
        InlineKeyboardButton shortBreakDuration = new InlineKeyboardButton("shortBreakDuration");
        InlineKeyboardButton longBreakDuration = new InlineKeyboardButton("longBreakDuration");
        InlineKeyboardButton longBreakInterval = new InlineKeyboardButton("longBreakInterval");
        InlineKeyboardButton isAutostartWork = new InlineKeyboardButton("isAutostartWork");
        InlineKeyboardButton isAutostartBreak = new InlineKeyboardButton("isAutostartBreak");
        InlineKeyboardButton closeButton = new InlineKeyboardButton("Close");

        workDuration.setCallbackData(BotState.TIMER_SETTINGS_WORK.getCommand());
        shortBreakDuration.setCallbackData(BotState.TIMER_SETTINGS_SBREAK.getCommand());
        longBreakDuration.setCallbackData(BotState.TIMER_SETTINGS_LBREAK.getCommand());
        longBreakInterval.setCallbackData(BotState.TIMER_SETTINGS_LBREAK_INTERVAL.getCommand());
        isAutostartWork.setCallbackData(BotState.TIMER_SETTINGS_AUTOSTART_WORK.getCommand());
        isAutostartBreak.setCallbackData(BotState.TIMER_SETTINGS_AUTOSTART_BREAK.getCommand());
        closeButton.setCallbackData("CLOSE");

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(Arrays.asList(workDuration));
        rowList.add(Arrays.asList(shortBreakDuration));
        rowList.add(Arrays.asList(longBreakDuration));
        rowList.add(Arrays.asList(longBreakInterval));
        rowList.add(Arrays.asList(isAutostartWork));
        rowList.add(Arrays.asList(isAutostartBreak));
        rowList.add(Arrays.asList(closeButton));

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    @Override
    public BotState getHandlerName() {
        return BotState.TIMER_SETTINGS;
    }
}
