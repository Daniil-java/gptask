package com.education.gptask.telegram.handlers.task;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.entities.task.Task;
import com.education.gptask.services.TaskService;
import com.education.gptask.services.UserService;
import com.education.gptask.telegram.TelegramBot;
import com.education.gptask.telegram.entities.BotState;
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
import java.util.Arrays;
import java.util.List;

@Component
@AllArgsConstructor
public class TaskHandler implements MessageHandler {
    private final TaskService taskService;
    private final TelegramBot telegramBot;
    private final UserService userService;

    /*
        Данная константа является стандартным значением
        строк в списке задач.
    */
    private static int LIST_PAGE_ROW_COUNT = 8;

    /*
        Данная константа является стандартным значением
        параметра taskId метода MessageHandler.getList(...).
    */
    private static long DEFAULT_TASK_GETLIST_ID = 0;
    @Override
    public BotApiMethod handle(Message message, UserEntity userEntity) {
        Long chatId = message.getChatId();
        int messageId = message.getMessageId();
        String userAnswer = message.getText();
        BotState botState = userEntity.getBotState();
        SendMessage replyMessage = new SendMessage(String.valueOf(chatId),
                "Что-то пошло не так ¯\\_(ツ)_/¯");

        if (botState.equals(BotState.TASK)) {
            userEntity.setBotState(BotState.TASK_LIST);
            if (userEntity.getLastUpdatedTaskMessageId() != null) {
                DeleteMessage deleteLastTaskMessage =
                        new DeleteMessage(
                                String.valueOf(chatId),
                                Math.toIntExact(userEntity.getLastUpdatedTaskMessageId())
                        );
                telegramBot.sendMessage(deleteLastTaskMessage);
            }
            DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), messageId);
            telegramBot.sendMessage(deleteMessage);

            List<Task> taskList = taskService.getTasksAfterIdLimited(userEntity.getId(), 0, LIST_PAGE_ROW_COUNT);
            Message futureMessage = telegramBot
                    .sendReturnedMessage(getList(taskList, chatId, 0));
            userEntity.setLastUpdatedTaskMessageId(Long.valueOf(futureMessage.getMessageId()));
            userService.updateUserEntity(userEntity);
            return null;
        }

        if (botState.equals(BotState.TASK_MAIN_MENU)) {
            EditMessageText editMessageText;
            List<Task> taskList;
            long id;
            if (userAnswer.contains("/next") || userAnswer.contains("/prev")) {
                if (userAnswer.contains("/next")) {
                    id = Long.parseLong(userAnswer.substring("/next".length()));
                    taskList = taskService.getTasksAfterIdLimited(userEntity.getId(), id, LIST_PAGE_ROW_COUNT);
                } else {
                    id = Long.parseLong(userAnswer.substring("/prev".length()));
                    taskList = taskService.getTasksBeforeIdLimited(userEntity.getId(), id, LIST_PAGE_ROW_COUNT);
                }

            } else {
                id = DEFAULT_TASK_GETLIST_ID;
                taskList = taskService
                        .getTasksAfterIdLimited(userEntity.getId(), id, LIST_PAGE_ROW_COUNT);
            }

            editMessageText = MessageTypeConverter
                    .convertSendToEdit(getList(taskList, chatId, id));
            editMessageText.setMessageId(Math.toIntExact(userEntity.getLastUpdatedTaskMessageId()));
            userEntity.setBotState(BotState.TASK_LIST);
            userService.updateUserEntity(userEntity);
            return editMessageText;
        }

        return replyMessage;
    }

    /*
        Метод getList(...) отвечает за предоставление информации
        о задачах, ввиде клавиатуры, в Телеграм.

        Парамет taskId отвечает за предоставление следующей страницы
        списка, начинающейся со значения идентификатора выше, чем
        данный параметр
    */
    public static SendMessage getList
            (List<Task> taskList, long chatId, long taskId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        if (taskList.isEmpty()) {
            sendMessage.setText("You don't have any task");
            sendMessage.setReplyMarkup(getInlineMessageButtons());
        } else {
            sendMessage.setText("LIST:");
            boolean isFirst = taskId == 0;
            sendMessage.setReplyMarkup(
                    getInlineMessageListTaskButtons(taskList, LIST_PAGE_ROW_COUNT, isFirst));
        }
        return sendMessage;
    }
    public static InlineKeyboardMarkup getInlineMessageListTaskButtons(
            List<Task> taskList, int rowCount, boolean isFirst
    ) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for (Task task: taskList) {
            InlineKeyboardButton taskButton = new InlineKeyboardButton(
                    String.format("[%s]: %s", task.getId(), task.getName())
            );
            taskButton.setCallbackData("/id" + task.getId());
            rowList.add(Arrays.asList(taskButton));
        }

        List<InlineKeyboardButton> row = new ArrayList<>();
        if (!isFirst) {
            InlineKeyboardButton prevButton = new InlineKeyboardButton("<<<");
            prevButton.setCallbackData("/prev" + taskList.get(0).getId());
            row.add(prevButton);
        }
        if (taskList.size() == rowCount) {
            InlineKeyboardButton nextButton = new InlineKeyboardButton(">>>");
            nextButton.setCallbackData("/next" + taskList.get(taskList.size() - 1).getId());
            row.add(nextButton);
        }

        rowList.add(row);

        InlineKeyboardButton createButton = new InlineKeyboardButton("Создать");
        createButton.setCallbackData(BotState.TASK_CREATE.getCommand());
        rowList.add(Arrays.asList(createButton));

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    private static InlineKeyboardMarkup getInlineMessageButtons() {
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
    public List<BotState> getHandlerListName() {
        return Arrays.asList(BotState.TASK, BotState.TASK_MAIN_MENU);
    }
}
