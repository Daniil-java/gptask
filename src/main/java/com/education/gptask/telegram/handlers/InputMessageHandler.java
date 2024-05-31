package com.education.gptask.telegram.handlers;

import com.education.gptask.entities.UserEntity;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.HashMap;
import java.util.Map;

@Component
public class InputMessageHandler {
    private Map<String, MessageHandler> map = new HashMap<>();

    public void register(String botState, MessageHandler messageHandler) {
        map.put(botState, messageHandler);
    }

    public SendMessage processInputMessage(Message message, UserEntity userEntity) {
        MessageHandler currentMessageHandler = map.get(userEntity.getBotState().name());
        return currentMessageHandler.handle(message, userEntity);
    }
}
