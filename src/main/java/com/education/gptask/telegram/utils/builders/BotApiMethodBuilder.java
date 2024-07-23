package com.education.gptask.telegram.utils.builders;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class BotApiMethodBuilder {
    public static EditMessageText makeEditMessageText(Long chatId, int messageId, String text) {
        return EditMessageText.builder()
                .chatId(chatId)
                .text(text)
                .messageId(messageId)
                .parseMode(ParseMode.HTML)
                .build();
    }

    public static EditMessageText makeEditMessageText(Long chatId, int messageId, String text, InlineKeyboardMarkup keyboard) {
        return EditMessageText.builder()
                .chatId(chatId)
                .text(text)
                .messageId(messageId)
                .replyMarkup(keyboard)
                .parseMode(ParseMode.HTML)
                .build();
    }

}
