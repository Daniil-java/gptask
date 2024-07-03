package com.education.gptask.telegram.handlers.task.creation;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.entities.task.Priority;
import com.education.gptask.entities.task.Status;
import com.education.gptask.entities.task.Task;
import com.education.gptask.services.TaskService;
import com.education.gptask.services.UserService;
import com.education.gptask.telegram.TelegramBot;
import com.education.gptask.telegram.enteties.BotState;
import com.education.gptask.telegram.handlers.MessageHandler;
import com.education.gptask.telegram.handlers.task.TaskMainMenuHandler;
import com.education.gptask.telegram.utils.builders.BotApiMethodBuilder;
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
import java.util.Arrays;
import java.util.List;

@Component
@AllArgsConstructor
public class TaskCreationHandler implements MessageHandler {
    private final UserService userService;
    private final TaskService taskService;
    private final TelegramBot telegramBot;
    private final TaskMainMenuHandler mainMenuHandler;

    @Override
    public BotApiMethod handle(Message message, UserEntity userEntity) {
        Long chatId = message.getChatId();
        int messageId = message.getMessageId();
        String userAnswer = message.getText();
        BotState botState = userEntity.getBotState();
        SendMessage replyMessage = new SendMessage(String.valueOf(chatId),
                "Что-то пошло не так ¯\\_(ツ)_/¯");

        if (botState.equals(BotState.TASK_CREATE)) {
            EditMessageText editMessageText = BotApiMethodBuilder
                    .makeEditMessageText(chatId, messageId, "Choose task priority");
            editMessageText.setReplyMarkup(getInlineMessageButtonsPriority());

            userEntity.setBotState(BotState.TASK_CREATE_PRIORITY);
            userEntity.setLastUpdatedTaskId((long)messageId);
            userService.updateUserEntity(userEntity);
            return editMessageText;
        }

        if (botState.equals(BotState.TASK_CREATE_PRIORITY)) {
            Priority priority = Priority.valueOf(userAnswer);
            Task task = taskService.createTask(
                    new Task()
                            .setUserEntity(userEntity)
                            .setPriority(priority)
                            .setStatus(Status.PLANNED)
            );
            userEntity.setLastUpdatedTaskId(task.getId());
            userEntity.setBotState(BotState.TASK_CREATE_NAME);
            userService.updateUserEntity(userEntity);

            return BotApiMethodBuilder
                    .makeEditMessageText(chatId, messageId, "Give a name for task");
        }


        if (botState.equals(BotState.TASK_CREATE_NAME)) {
            Task task = taskService.getTaskById(userEntity.getLastUpdatedTaskId());
            String name = userAnswer, comment = null;
            if (!userAnswer.isEmpty() && userAnswer.indexOf("#") != -1) {
                name = userAnswer.substring(0, userAnswer.indexOf("#"));
                comment = userAnswer.substring(userAnswer.indexOf("#") + 1, userAnswer.length());
            }
            task.setName(name)
                    .setComment(comment);
            taskService.createTask(task);
            userEntity.setBotState(BotState.TASK_LIST);
            userEntity.setLastUpdatedTaskId(null);
            userService.updateUserEntity(userEntity);

            DeleteMessage deleteMessage = new DeleteMessage(chatId.toString(), messageId);
            telegramBot.sendMessage(deleteMessage);
            return mainMenuHandler.handle(message, userEntity);
        }

        return replyMessage;
    }

    public static InlineKeyboardMarkup getInlineMessageButtonsPriority() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton mustButton = new InlineKeyboardButton("MUST");
        InlineKeyboardButton shouldButton = new InlineKeyboardButton("SHOULD");
        InlineKeyboardButton couldButton = new InlineKeyboardButton("COULD");
        InlineKeyboardButton wouldButton = new InlineKeyboardButton("WOULD");

        mustButton.setCallbackData("MUST");
        shouldButton.setCallbackData("SHOULD");
        couldButton.setCallbackData("COULD");
        wouldButton.setCallbackData("WOULD");

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(mustButton);
        row1.add(shouldButton);
        row1.add(couldButton);
        row1.add(wouldButton);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(row1);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup getInlineMessageButtonsPriority(long taskId) {
        InlineKeyboardMarkup keyboardMarkup = getInlineMessageButtonsPriority();
        for (InlineKeyboardButton button: keyboardMarkup.getKeyboard().get(0)) {
            button.setCallbackData(button.getCallbackData() + "_" + taskId);
        }
        return keyboardMarkup;
    }

    @Override
    public List<BotState> getHandlerListName() {
        return Arrays.asList(BotState.TASK_CREATE, BotState.TASK_CREATE_NAME, BotState.TASK_CREATE_PRIORITY);
    }
}
