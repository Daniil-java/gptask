package com.education.gptask.telegram.facades;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.services.UserService;
import com.education.gptask.telegram.entities.BotState;
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
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            log.info("New callbackQuery from User: {}, userId: {}, with data: {}",
                    update.getCallbackQuery().getFrom().getUserName(),
                    callbackQuery.getFrom().getId(), update.getCallbackQuery().getData());

            Message message = callbackQuery.getMessage();
            message.setMessageId(callbackQuery.getMessage().getMessageId());
            message.setFrom(callbackQuery.getFrom());
            message.setText(callbackQuery.getData());
            return handleInputMessage(message);
        } else {
            Message message = update.getMessage();
            log.info("New message from User:{}, chatId: {}, messageId: {},  with text: {}",
                    message.getFrom().getUserName(),
                    message.getChatId(),
                    message.getMessageId(),
                    message.getText()
            );
            return handleInputMessage(message);
        }
    }

    private BotApiMethod handleInputMessage(Message message) {
        String inputMsg = message.getText();
        BotState botState = null;
        if (!BotState.fromCommand(inputMsg).equals(BotState.PROCESSING)) {
            botState = BotState.fromCommand(message.getText());
        }
        UserEntity userEntity = userService.getOrCreateUser(message.getFrom(), botState);
        return inputMessageHandler.processInputMessage(message, userEntity);
    }

}
