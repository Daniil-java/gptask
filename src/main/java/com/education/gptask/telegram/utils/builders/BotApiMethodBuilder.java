package com.education.gptask.telegram.utils.builders;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

public class BotApiMethodBuilder {
    public static EditMessageText makeEditMessageText(Long chatId, int messageId, String text) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(messageId);
        editMessageText.setText(text);
        return editMessageText;
    }

}
