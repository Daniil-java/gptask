package com.education.gptask.telegram.handlers.start;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.telegram.entities.BotState;
import com.education.gptask.telegram.handlers.MessageHandler;
import com.education.gptask.telegram.services.LocaleMessageService;
import com.education.gptask.telegram.utils.builders.BotApiMethodBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;
import java.util.List;

@Component
public class StartHandler implements MessageHandler {
    @Autowired
    private LocaleMessageService localeMessageService;
    private static final String START_MESSAGE = "reply.start";

    @Override
    public SendMessage handle(Message message, UserEntity userEntity) {
        Long chatId = message.getChatId();
        BotState botState = userEntity.getBotState();
        SendMessage replyMessage = BotApiMethodBuilder.makeSendMessage(chatId);

        if (BotState.START.equals(botState)) {
            replyMessage.setText(localeMessageService.getMessage(START_MESSAGE));
        }

        return replyMessage;
    }

    @Override
    public List<BotState> getHandlerListName() {
        return Arrays.asList(BotState.START);
    }

}
