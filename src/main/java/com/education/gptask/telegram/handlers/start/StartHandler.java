package com.education.gptask.telegram.handlers.start;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.telegram.enteties.BotState;
import com.education.gptask.telegram.handlers.MessageHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class StartHandler implements MessageHandler {
    @Override
    public SendMessage handle(Message message, UserEntity userEntity) {
        Long chatId = message.getChatId();
        BotState botState = userEntity.getBotState();
        SendMessage replyMessage = new SendMessage(String.valueOf(chatId),
                "Что-то пошло не так ¯\\_(ツ)_/¯");

        if (botState.equals(BotState.START)) {
            replyMessage.setText("Hi! You can use: \n/task\n/timer");
        }

        return replyMessage;
    }

    @Override
    public BotState getHandlerName() {
        return BotState.START;
    }

}
