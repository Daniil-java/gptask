package com.education.gptask.telegram.handlers.timer;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.entities.timer.Timer;
import com.education.gptask.services.TimerService;
import com.education.gptask.telegram.enteties.BotState;
import com.education.gptask.telegram.handlers.MessageHandler;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
    public SendMessage handle(Message message, UserEntity userEntity) {
        Long chatId = message.getChatId();
        String userAnswer = message.getText();
        Long userId = message.getFrom().getId();
        BotState botState = userEntity.getBotState();
        SendMessage replyMessage = new SendMessage(String.valueOf(chatId),
                "Что-то пошло не так ¯\\_(ツ)_/¯");

        if (botState.equals(BotState.TIMER)) {
            Timer timer = timerService.getTimersByUserId(userEntity.getId()).get(0);

            replyMessage.setReplyMarkup(getInlineMessageButtons());
            replyMessage.setText(timer.getStatus().name());
        }

        return replyMessage;
    }

    private InlineKeyboardMarkup getInlineMessageButtons() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton startButton = new InlineKeyboardButton("Старт");
        InlineKeyboardButton pauseButton = new InlineKeyboardButton("Пауза");
        InlineKeyboardButton stopButton = new InlineKeyboardButton("Стоп");

        startButton.setCallbackData(BotState.TIMER_START.getCommand());
        pauseButton.setCallbackData(BotState.TIMER_PAUSE.getCommand());
        stopButton.setCallbackData(BotState.TIMER_STOP.getCommand());

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row1.add(startButton);
        row1.add(pauseButton);

        row2.add(stopButton);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(row1);
        rowList.add(row2);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    @Override
    public BotState getHandlerName() {
        return BotState.TIMER;
    }
}
