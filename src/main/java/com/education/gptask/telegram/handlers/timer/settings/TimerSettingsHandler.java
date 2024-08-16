package com.education.gptask.telegram.handlers.timer.settings;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.entities.timer.Timer;
import com.education.gptask.services.TimerService;
import com.education.gptask.services.UserService;
import com.education.gptask.telegram.TelegramBot;
import com.education.gptask.telegram.entities.BotState;
import com.education.gptask.telegram.handlers.MessageHandler;
import com.education.gptask.telegram.handlers.timer.TimerHandler;
import com.education.gptask.telegram.services.LocaleMessageService;
import com.education.gptask.telegram.utils.builders.BotApiMethodBuilder;
import com.education.gptask.telegram.utils.converters.NumeralConverter;
import com.education.gptask.telegram.utils.keyboards.InlineKeyboardBuilder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
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
    private final LocaleMessageService localeMessageService;
    private static final String VALUE_WAITING_MESSAGE = "reply.general.value-waiting";
    private static final String CLOSE_MESSAGE = "reply.general.close";
    private static final String WORK_DURATION_MESSAGE = "reply.timer.workDuration";
    private static final String SHORT_BREAK_DURATION_MESSAGE = "reply.timer.shortBreakDuration";
    private static final String LONG_BREAK_DURATION_MESSAGE = "reply.timer.longBreakDuration";
    private static final String LONG_BREAK_INTERVAL_MESSAGE = "reply.timer.longBreakInterval";
    private static final String IS_AUTOSTART_WORK_MESSAGE = "reply.timer.isAutostartWork";
    private static final String IS_AUTOSTART_BREAK_MESSAGE = "reply.timer.isAutostartBreak";
    private static final String CHOICE_MESSAGE = "reply.general.choice";
    private static final String BACK_COMMAND = "command.timer.back";

    @Override
    public BotApiMethod handle(Message message, UserEntity userEntity) {
        Long chatId = message.getChatId();
        int messageId = message.getMessageId();
        String userAnswer = message.getText();
        BotState botState = userEntity.getBotState();

        Timer timer = timerService.getAnyNotCompleteTimerByUserId(userEntity.getId()).get(0);
        EditMessageText editMessageText = BotApiMethodBuilder.makeEditMessageText(chatId, messageId,
                localeMessageService.getMessage(localeMessageService.getMessage(VALUE_WAITING_MESSAGE)));

        if (BotState.TIMER_SETTINGS.equals(botState)) {
            if (userAnswer.equals(localeMessageService.getMessage(BACK_COMMAND))) {
                userEntity.setBotState(BotState.TIMER_STATUS);
                userService.updateUserEntity(userEntity);
                return timerHandler.handle(message, userEntity);
            }
            editMessageText.setText(getTimerSettingsInfo(timer));
            editMessageText.setReplyMarkup(getInlineMessageButtons());
            return editMessageText;
        }

        if (Arrays.asList(BotState.TIMER_SETTINGS_AUTOSTART_BREAK, BotState.TIMER_SETTINGS_AUTOSTART_WORK).contains(botState)) {
            if (userAnswer.equals("true") || userAnswer.equals("false")) {
                if (botState.equals(BotState.TIMER_SETTINGS_AUTOSTART_BREAK)) {
                    timer.setAutostartBreak(Boolean.valueOf(userAnswer));
                } else if (botState.equals(BotState.TIMER_SETTINGS_AUTOSTART_WORK)) {
                    timer.setAutostartWork(Boolean.valueOf(userAnswer));
                }
            } else {
                editMessageText.setText(localeMessageService.getMessage(CHOICE_MESSAGE));
                editMessageText.setReplyMarkup(InlineKeyboardBuilder.getTrueOrFalseK());
                return editMessageText;
            }
        } else {
            int value = NumeralConverter.parsePositiveSafelyInt(userAnswer);
            if (value > -1) {
                switch (botState) {
                    case TIMER_SETTINGS_LBREAK:
                        timer.setLongBreakDuration(value);
                        break;
                    case TIMER_SETTINGS_WORK:
                        timer.setWorkDuration(value);
                        break;
                    case TIMER_SETTINGS_SBREAK:
                        timer.setShortBreakDuration(value);
                        break;
                    case TIMER_SETTINGS_LBREAK_INTERVAL:
                        timer.setLongBreakInterval(value);
                        break;
                }
            } else {
                return editMessageText;
            }
        }

        if (!BotState.TIMER_SETTINGS.equals(botState)) {
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

        return BotApiMethodBuilder.makeSendMessage(chatId);
    }

    private static String getTimerSettingsInfo(Timer timer) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\uD83D\uDCBC ").append("<strong>Рабочее время: </strong>").append(timer.getWorkDuration()).append(" мин.").append("\n");
        stringBuilder.append("⏸ ").append("<strong>Короткая пауза: </strong>").append(timer.getShortBreakDuration()).append(" мин.").append("\n");
        stringBuilder.append("☕ ").append("<strong>Длинная пауза: </strong>").append(timer.getLongBreakDuration()).append(" мин.").append("\n");
        stringBuilder.append("\uD83D\uDD01 ").append("<strong>Интервал длинной паузы: </strong>").append(timer.getLongBreakInterval()).append("\n");
        if (timer.isAutostartWork()) {
            stringBuilder.append("✔ ").append("<strong>Автостарт таймера работы: </strong>ON").append("\n");
        } else {
            stringBuilder.append("❌ ").append("<strong>Автостарт таймера работы: </strong>OFF").append("\n");
        }

        if (timer.isAutostartBreak()) {
            stringBuilder.append("✔ ").append("<strong>Автостарт таймера паузы: </strong>ON").append("\n");
        } else {
            stringBuilder.append("❌ ").append("<strong>Автостарт таймера паузы: </strong>OFF").append("\n");
        }

        return stringBuilder.toString();
    }

    private InlineKeyboardMarkup getInlineMessageButtons() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton workDuration = new InlineKeyboardButton(localeMessageService.getMessage(WORK_DURATION_MESSAGE));
        InlineKeyboardButton shortBreakDuration = new InlineKeyboardButton(localeMessageService.getMessage(SHORT_BREAK_DURATION_MESSAGE));
        InlineKeyboardButton longBreakDuration = new InlineKeyboardButton(localeMessageService.getMessage(LONG_BREAK_DURATION_MESSAGE));
        InlineKeyboardButton longBreakInterval = new InlineKeyboardButton(localeMessageService.getMessage(LONG_BREAK_INTERVAL_MESSAGE));
        InlineKeyboardButton isAutostartWork = new InlineKeyboardButton(localeMessageService.getMessage(IS_AUTOSTART_WORK_MESSAGE));
        InlineKeyboardButton isAutostartBreak = new InlineKeyboardButton(localeMessageService.getMessage(IS_AUTOSTART_BREAK_MESSAGE));
        InlineKeyboardButton closeButton = new InlineKeyboardButton(localeMessageService.getMessage(CLOSE_MESSAGE));

        workDuration.setCallbackData(BotState.TIMER_SETTINGS_WORK.getCommand());
        shortBreakDuration.setCallbackData(BotState.TIMER_SETTINGS_SBREAK.getCommand());
        longBreakDuration.setCallbackData(BotState.TIMER_SETTINGS_LBREAK.getCommand());
        longBreakInterval.setCallbackData(BotState.TIMER_SETTINGS_LBREAK_INTERVAL.getCommand());
        isAutostartWork.setCallbackData(BotState.TIMER_SETTINGS_AUTOSTART_WORK.getCommand());
        isAutostartBreak.setCallbackData(BotState.TIMER_SETTINGS_AUTOSTART_BREAK.getCommand());
        closeButton.setCallbackData(BACK_COMMAND);

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
