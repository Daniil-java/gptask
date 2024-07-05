package com.education.gptask.telegram.handlers.notification;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.telegram.entities.BotState;
import com.education.gptask.telegram.handlers.MessageHandler;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;
import java.util.List;

@Component
@AllArgsConstructor
public class NotificationHandler implements MessageHandler {
    @Override
    public BotApiMethod handle(Message message, UserEntity userEntity) {
        return new DeleteMessage(String.valueOf(message.getChatId()), message.getMessageId());
    }

    @Override
    public List<BotState> getHandlerListName() {
        return Arrays.asList(BotState.NOTIFICATION_TIMER_ALERT);
    }
}
