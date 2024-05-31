package com.education.gptask.telegram.handlers;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.telegram.enteties.BotState;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface MessageHandler {
    SendMessage handle(Message message, UserEntity userEntity);
    BotState getHandlerName();

    @Autowired
    default void registerMyself(InputMessageHandler inputMessageHandler) {
        inputMessageHandler.register(getHandlerName().name(), this);
    }

}
