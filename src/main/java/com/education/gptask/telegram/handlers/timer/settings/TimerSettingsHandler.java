package com.education.gptask.telegram.handlers.timer.settings;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.entities.timer.Timer;
import com.education.gptask.services.TimerService;
import com.education.gptask.services.UserService;
import com.education.gptask.telegram.TelegramBot;
import com.education.gptask.telegram.entities.BotState;
import com.education.gptask.telegram.handlers.MessageHandler;
import com.education.gptask.telegram.handlers.timer.TimerHandler;
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
    private final TimerHandler timerHandler;

    @Override
    public BotApiMethod handle(Message message, UserEntity userEntity) {
        Long chatId = message.getChatId();
        int messageId = message.getMessageId();
        String userAnswer = message.getText();
        BotState botState = userEntity.getBotState();
        SendMessage replyMessage = new SendMessage(String.valueOf(chatId),
                "Что-то пошло не так ¯\\_(ツ)_/¯");


        Timer timer = timerService.getTimersByUserId(userEntity.getId()).get(0);
        EditMessageText editMessageText = BotApiMethodBuilder
                .makeEditMessageText(chatId,
                        timer.getTelegramMessageId(),
                        "Enter value"
                );
        if (botState.equals(BotState.TIMER_SETTINGS)) {
            if (userAnswer.equals("/back")) {
                userEntity.setBotState(BotState.TIMER_STATUS);
                userService.updateUserEntity(userEntity);
                return timerHandler.handle(message, userEntity);
            }
            editMessageText.setText(getTimerSettingsInfo(timer));
            editMessageText.setReplyMarkup(getInlineMessageButtons());
            return editMessageText;
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
            if (!userAnswer.isEmpty() && !userAnswer.equals(BotState.TIMER_SETTINGS_LBREAK.getCommand())) {
                int duration = tryToParseIntPositive(userAnswer);
                if (duration == -1) return replyMessage;
                timer.setLongBreakDuration(duration);
            } else return editMessageText;
        }

        if (botState.equals(BotState.TIMER_SETTINGS_LBREAK_INTERVAL)) {
            if (!userAnswer.isEmpty() && !userAnswer.equals(BotState.TIMER_SETTINGS_LBREAK_INTERVAL.getCommand())) {
                int interval = tryToParseIntPositive(userAnswer);
                if (interval == -1) return replyMessage;
                timer.setLongBreakInterval(interval);
            } else {
                return editMessageText;
            }
        }

        if (botState.equals(BotState.TIMER_SETTINGS_WORK)) {
            if (!userAnswer.isEmpty() && !userAnswer.equals(BotState.TIMER_SETTINGS_WORK.getCommand())) {
                int duration = tryToParseIntPositive(userAnswer);
                if (duration == -1) return replyMessage;
                timer.setWorkDuration(duration);
            } else return editMessageText;
        }

        if (botState.equals(BotState.TIMER_SETTINGS_SBREAK)) {
            if (!userAnswer.isEmpty() && !userAnswer.equals(BotState.TIMER_SETTINGS_SBREAK.getCommand())) {
                int duration = tryToParseIntPositive(userAnswer);
                if (duration == -1) return replyMessage;
                timer.setShortBreakDuration(duration);
            } else return editMessageText;
        }
        if (!botState.equals(BotState.TIMER_SETTINGS)) {
            if (!(botState.equals(BotState.TIMER_SETTINGS_AUTOSTART_WORK) || botState.equals(BotState.TIMER_SETTINGS_AUTOSTART_BREAK))) {
                DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), messageId);
                telegramBot.sendMessage(deleteMessage);
            }

            timer = timerService.updateTimer(timer);
            userEntity.setBotState(BotState.TIMER_SETTINGS);
            userService.updateUserEntity(userEntity);
            editMessageText.setText(getTimerSettingsInfo(timer));
            editMessageText.setReplyMarkup(getInlineMessageButtons());

            return editMessageText;
        }

        return replyMessage;
    }

    private static String getTimerSettingsInfo(Timer timer) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\uD83D\uDCBC ").append("Рабочее время: ").append(timer.getWorkDuration()).append(" мин.").append("\n");
        stringBuilder.append("⏸ ").append("Время короткой паузы: ").append(timer.getShortBreakDuration()).append(" мин.").append("\n");
        stringBuilder.append("☕ ").append("Время длинной паузы: ").append(timer.getLongBreakDuration()).append(" мин.").append("\n");
        stringBuilder.append("\uD83D\uDD01 ").append("Интервал длинной паузы: ").append(timer.getLongBreakInterval()).append("\n");
        if (timer.isAutostartWork()) {
            stringBuilder.append("✔ ").append("Автостарт таймера работы: ON").append("\n");
        } else {
            stringBuilder.append("❌ ").append("Автостарт таймера работы: OFF").append("\n");
        }

        if (timer.isAutostartBreak()) {
            stringBuilder.append("✔ ").append("Автостарт таймера паузы: ON").append("\n");
        } else {
            stringBuilder.append("❌ ").append("Автостарт таймера паузы: OFF").append("\n");
        }

        return stringBuilder.toString();
    }

    private int tryToParseIntPositive(String str) {
        int integer;
        try {
            integer = Integer.parseInt(str);
            if (integer < 0) return -1;
            return integer;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private InlineKeyboardMarkup getInlineMessageButtons() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton workDuration = new InlineKeyboardButton("Рабочее время");
        InlineKeyboardButton shortBreakDuration = new InlineKeyboardButton("Время короткой паузы");
        InlineKeyboardButton longBreakDuration = new InlineKeyboardButton("Время длинной паузы");
        InlineKeyboardButton longBreakInterval = new InlineKeyboardButton("Интервал длинной паузы");
        InlineKeyboardButton isAutostartWork = new InlineKeyboardButton("Автостарт таймера работы");
        InlineKeyboardButton isAutostartBreak = new InlineKeyboardButton("Автостарт таймера паузы");
        InlineKeyboardButton closeButton = new InlineKeyboardButton("Закрыть");

        workDuration.setCallbackData(BotState.TIMER_SETTINGS_WORK.getCommand());
        shortBreakDuration.setCallbackData(BotState.TIMER_SETTINGS_SBREAK.getCommand());
        longBreakDuration.setCallbackData(BotState.TIMER_SETTINGS_LBREAK.getCommand());
        longBreakInterval.setCallbackData(BotState.TIMER_SETTINGS_LBREAK_INTERVAL.getCommand());
        isAutostartWork.setCallbackData(BotState.TIMER_SETTINGS_AUTOSTART_WORK.getCommand());
        isAutostartBreak.setCallbackData(BotState.TIMER_SETTINGS_AUTOSTART_BREAK.getCommand());
        closeButton.setCallbackData("/back");

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
    public List<BotState> getHandlerListName() {
        return Arrays.asList(
                BotState.TIMER_SETTINGS,
                BotState.TIMER_SETTINGS_WORK, BotState.TIMER_SETTINGS_LBREAK,
                BotState.TIMER_SETTINGS_LBREAK_INTERVAL, BotState.TIMER_SETTINGS_AUTOSTART_WORK,
                BotState.TIMER_SETTINGS_AUTOSTART_BREAK, BotState.TIMER_SETTINGS_SBREAK);
    }
}
