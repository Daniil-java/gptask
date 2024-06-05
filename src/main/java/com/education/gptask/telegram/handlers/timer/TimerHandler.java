package com.education.gptask.telegram.handlers.timer;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.entities.timer.Timer;
import com.education.gptask.entities.timer.TimerStatus;
import com.education.gptask.services.TimerService;
import com.education.gptask.telegram.enteties.BotState;
import com.education.gptask.telegram.handlers.MessageHandler;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class TimerHandler implements MessageHandler {
    private final TimerService timerService;
    @Override
    public BotApiMethod handle(Message message, UserEntity userEntity) {
        Long chatId = message.getChatId();
        int messageId = message.getMessageId();
        BotState botState = userEntity.getBotState();
        SendMessage replyMessage = new SendMessage(String.valueOf(chatId),
                "Что-то пошло не так ¯\\_(ツ)_/¯");

        Timer timer = timerService.getTimersByUserId(userEntity.getId(), messageId).get(0);

        if (botState.equals(BotState.TIMER) || botState.equals(BotState.TIMER_STOP)) {
            replyMessage.setReplyMarkup(getInlineMessageButtons());
            replyMessage.setText(timer.getStatus().name());
        }

        if (botState.equals(BotState.TIMER_STOP)) {
            timer = timerService.updateTimerStatus(timer.getId(), TimerStatus.PENDING.name());
            EditMessageText editMessageText = makeEditMessageText(chatId, messageId, timer);
            editMessageText.setReplyMarkup(getInlineMessageButtons());
            return editMessageText;
        }

        if (botState.equals(BotState.TIMER_START)) {
            timer = timerService.updateTimerStatus(timer.getId(), TimerStatus.RUNNING.name());
            EditMessageText editMessageText = makeEditMessageText(chatId, messageId, timer);
            editMessageText.setReplyMarkup(TimerStartHandler.getInlineMessageButtons());
            return editMessageText;
        }

        if (botState.equals(BotState.TIMER_PAUSE)) {
            timer = timerService.updateTimerStatus(timer.getId(), TimerStatus.PAUSED.name());
            EditMessageText editMessageText = makeEditMessageText(chatId, messageId, timer);
            editMessageText.setReplyMarkup(TimerPauseHandler.getInlineMessageButtons());
            return editMessageText;
        }

        return replyMessage;
    }

    private EditMessageText makeEditMessageText(Long chatId, int messageId, Timer timer) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(messageId);
        editMessageText.setText(timer.getStatus().name());
        return editMessageText;
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

    @Override
    public BotState getHandlerName() {
        return BotState.TIMER;
    }
}
