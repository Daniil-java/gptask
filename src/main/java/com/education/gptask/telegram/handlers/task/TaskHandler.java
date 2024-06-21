package com.education.gptask.telegram.handlers.task;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.entities.task.Task;
import com.education.gptask.services.TaskService;
import com.education.gptask.services.UserService;
import com.education.gptask.telegram.TelegramBot;
import com.education.gptask.telegram.enteties.BotState;
import com.education.gptask.telegram.handlers.MessageHandler;
import com.education.gptask.telegram.utils.converters.MessageTypeConverter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class TaskHandler implements MessageHandler {
    private final TaskService taskService;
    private final TelegramBot telegramBot;
    private final UserService userService;
    @Override
    public BotApiMethod handle(Message message, UserEntity userEntity) {
        Long chatId = message.getChatId();
        int messageId = message.getMessageId();
        BotState botState = userEntity.getBotState();
        SendMessage replyMessage = new SendMessage(String.valueOf(chatId),
                "Что-то пошло не так ¯\\_(ツ)_/¯");

        if (botState.equals(BotState.TASK)) {
            userEntity.setBotState(BotState.TASK_LIST);
            DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), messageId);
            DeleteMessage deleteLastTaskMessage =
                    new DeleteMessage(
                            String.valueOf(chatId),
                            Math.toIntExact(userEntity.getLastUpdatedTaskMessageId())
                    );
            telegramBot.sendMessage(deleteMessage);
            telegramBot.sendMessage(deleteLastTaskMessage);
            Message futureMessage = telegramBot.sendReturnedMessage(getList(userEntity, chatId));
            userEntity.setLastUpdatedTaskMessageId(Long.valueOf(futureMessage.getMessageId()));
            userService.updateUserEntity(userEntity);
            return null;
        }
        if (botState.equals(BotState.TASK_MAIN_MENU)) {
            EditMessageText editMessageText = MessageTypeConverter.convertSendToEdit(getList(userEntity, chatId));
            editMessageText.setMessageId(Math.toIntExact(userEntity.getLastUpdatedTaskMessageId()));
            return editMessageText;
        }

        return replyMessage;
    }
    public SendMessage getList(UserEntity userEntity, long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        List<Task> taskList = taskService.getTasksByUserId(userEntity.getId());
        if (taskList.isEmpty()) {
            sendMessage.setText("You don't have any task");
            sendMessage.setReplyMarkup(getInlineMessageButtons());
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (Task task: taskList) {
                stringBuilder.append(task.getId()).append("\n");
            }
            sendMessage.setText(stringBuilder.toString());
            sendMessage.setReplyMarkup(getInlineMessageButtons());
        }
        return sendMessage;
    }

    private InlineKeyboardMarkup getInlineMessageButtons() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton createButton = new InlineKeyboardButton("Создать");

        createButton.setCallbackData(BotState.TASK_CREATE.getCommand());

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(row1);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    @Override
    public BotState getHandlerName() {
        return BotState.TASK;
    }
}
