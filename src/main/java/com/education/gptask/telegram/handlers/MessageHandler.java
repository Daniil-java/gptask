package com.education.gptask.telegram.handlers;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.telegram.entities.BotState;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

public interface MessageHandler {
    BotApiMethod handle(Message message, UserEntity userEntity);

    List<BotState> getHandlerListName();

    @Autowired
    default void registerMyself(InputMessageHandler inputMessageHandler) {
        List<BotState> stateList = getHandlerListName();
        for (BotState state: stateList) {
            inputMessageHandler.register(state.name(), this);
        }
    }

}
