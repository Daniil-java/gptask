package com.education.gptask.telegram.facades;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.services.UserService;
import com.education.gptask.telegram.enteties.BotState;
import com.education.gptask.telegram.handlers.InputMessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageFacade {
    private final UserService userService;
    private final InputMessageHandler inputMessageHandler;

    public BotApiMethod handleUpdate(Update update) {
        BotApiMethod replyMessage = null;
        Message message = null;

        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            log.info("New callbackQuery from User: {}, userId: {}, with data: {}",
                    update.getCallbackQuery().getFrom().getUserName(),
                    callbackQuery.getFrom().getId(), update.getCallbackQuery().getData());

            message = callbackQuery.getMessage();
            message.setText(callbackQuery.getData());
            message.setFrom(callbackQuery.getFrom());
            message.setMessageId(callbackQuery.getMessage().getMessageId());
        } else {
            message = update.getMessage();
            log.info("New message from User:{}, chatId: {}, messageId: {},  with text: {}",
                    message.getFrom().getUserName(),
                    message.getChatId(),
                    message.getMessageId(),
                    message.getText()
            );
        }
        if (message != null && message.hasText()) {
            replyMessage = handleInputMessage(message);
        }

        return replyMessage;
    }

    private BotApiMethod handleInputMessage(Message message) {
        String inputMsg = message.getText();
        BotState botState = BotState.fromCommand(inputMsg);
        UserEntity userEntity = userService.getOrCreateUser(message.getFrom(), botState);
        return inputMessageHandler.processInputMessage(message, userEntity);
    }

}
