package com.education.gptask.telegram.utils.converters;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class MessageTypeConverter {

    public static SendMessage convertEditToSend(EditMessageText editMessageText) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(editMessageText.getChatId());
        sendMessage.setText(editMessageText.getText());
        sendMessage.setReplyMarkup(editMessageText.getReplyMarkup());

        return sendMessage;
    }

    public static EditMessageText convertSendToEdit(SendMessage sendMessage) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(sendMessage.getChatId());
        editMessageText.setText(sendMessage.getText());
        if (sendMessage.getReplyMarkup() instanceof InlineKeyboardMarkup) {
            editMessageText.setReplyMarkup((InlineKeyboardMarkup) sendMessage.getReplyMarkup());
        }

        return editMessageText;
    }
}
