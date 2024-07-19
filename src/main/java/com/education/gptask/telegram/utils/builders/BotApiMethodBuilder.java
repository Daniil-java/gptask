package com.education.gptask.telegram.utils.builders;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

public class BotApiMethodBuilder {
    public static EditMessageText makeEditMessageText(Long chatId, int messageId, String text) {

        return EditMessageText.builder()
                .chatId(chatId)
                .text(text)
                .messageId(messageId)
                .build();
    }

}
