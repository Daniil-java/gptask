package com.education.gptask.telegram.handlers.task.list;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.entities.task.Priority;
import com.education.gptask.entities.task.Status;
import com.education.gptask.entities.task.Task;
import com.education.gptask.services.TaskService;
import com.education.gptask.services.TimerService;
import com.education.gptask.services.UserService;
import com.education.gptask.telegram.TelegramBot;
import com.education.gptask.telegram.enteties.BotState;
import com.education.gptask.telegram.handlers.MessageHandler;
import com.education.gptask.telegram.handlers.task.TaskMainMenuHandler;
import com.education.gptask.telegram.handlers.task.creation.TaskCreationHandler;
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
public class TaskListHandler implements MessageHandler {
    private final UserService userService;
    private final TelegramBot telegramBot;
    private final TaskService taskService;
    private final TimerService timerService;
    private final TaskMainMenuHandler mainMenuHandler;

    @Override
    public BotApiMethod handle(Message message, UserEntity userEntity) {
        Long chatId = message.getChatId();
        int messageId = message.getMessageId();
        String userAnswer = message.getText();
        BotState botState = userEntity.getBotState();
        SendMessage replyMessage = new SendMessage(String.valueOf(chatId),
                "Что-то пошло не так ¯\\_(ツ)_/¯");

        if (botState.equals(BotState.TASK_LIST)) {
            if (!userAnswer.isEmpty() && userAnswer.matches("[0-9]+")) {
                long taskId = Long.parseLong(userAnswer);
                Task task = taskService.getTaskById(taskId);
                EditMessageText editMessageText = BotApiMethodBuilder
                        .makeEditMessageText(chatId, Math.toIntExact(userEntity.getLastUpdatedTaskMessageId()), task.toString());
                editMessageText.setReplyMarkup(getInlineMessageButtons(taskId));
                DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), messageId);
                telegramBot.sendMessage(deleteMessage);
                return editMessageText;
            }
            if (!userAnswer.isEmpty() && userAnswer.startsWith("/delete")) {
                long taskId = Long.parseLong(userAnswer.substring("/delete".length()));
                taskService.deleteTaskById(taskId);
                return mainMenuHandler.handle(message, userEntity);
            }
            if (!userAnswer.isEmpty() && userAnswer.startsWith("/add")) {
                long taskId = Long.parseLong(userAnswer.substring("/add".length()));
                long timerId = timerService.getTimersByUserId(userEntity.getId()).get(0).getId();
                timerService.bindTaskToTimer(timerId, taskId);
                return new SendMessage(String.valueOf(chatId), "Task has has added to timer!");
            }
            if (!userAnswer.isEmpty() && userAnswer.startsWith("/done")) {
                long taskId = Long.parseLong(userAnswer.substring("/done".length()));
                Task task = taskService.getTaskById(taskId);
                task.setStatus(Status.DONE);
                taskService.createTask(task);
                EditMessageText editMessageText = BotApiMethodBuilder.makeEditMessageText(chatId, Math.toIntExact(userEntity.getLastUpdatedTaskMessageId()), task.toString());
                editMessageText.setReplyMarkup(getInlineMessageButtons(taskId));
                return editMessageText;
            }

            if (!userAnswer.isEmpty() && userAnswer.startsWith("/subtask")) {
                long taskId = Long.parseLong(userAnswer.substring("/subtask".length()));
                EditMessageText editMessageText = BotApiMethodBuilder
                        .makeEditMessageText(
                                chatId,
                                Math.toIntExact(userEntity.getLastUpdatedTaskMessageId()),
                                "Choose priority"
                        );
                editMessageText.setReplyMarkup(TaskCreationHandler.getInlineMessageButtonsPriority(taskId));
                return editMessageText;
            }
            if (!userAnswer.isEmpty() && userAnswer.matches("^(MUST|SHOULD|COULD|WOULD).*")) {
                long taskId = Long.parseLong(userAnswer.substring(userAnswer.indexOf("_") + 1));

                String priority = userAnswer.substring(0, userAnswer.indexOf("_"));

                Task task = taskService.createTask(
                        new Task()
                                .setUserEntity(userEntity)
                                .setPriority(Priority.valueOf(priority))
                                .setStatus(Status.PLANNED)
                                .setParent(new Task().setId(taskId))
                );
                userEntity.setLastUpdatedTaskId(task.getId());
                userEntity.setBotState(BotState.TASK_CREATE_SUBTASK);
                userService.updateUserEntity(userEntity);

                EditMessageText editMessageText = BotApiMethodBuilder
                        .makeEditMessageText(
                                chatId,
                                Math.toIntExact(userEntity.getLastUpdatedTaskMessageId()),
                                "Give a name#comment"
                        );
                return editMessageText;
            }

        }
        if (botState.equals(BotState.TASK_CREATE_SUBTASK)) {
            Task task = taskService.getTaskById(userEntity.getLastUpdatedTaskId());
            String name = userAnswer, comment = null;
            if (!userAnswer.isEmpty() && userAnswer.indexOf("#") != -1) {
                name = userAnswer.substring(0, userAnswer.indexOf("#"));
                comment = userAnswer.substring(userAnswer.indexOf("#") + 1, userAnswer.length() - 1);
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

    private InlineKeyboardMarkup getInlineMessageButtons(long taskId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton backButton = new InlineKeyboardButton("Назад");
        InlineKeyboardButton deleteButton = new InlineKeyboardButton("Удалить");
        InlineKeyboardButton addToTimerButton = new InlineKeyboardButton("Добавить в таймер");
        InlineKeyboardButton doneButton = new InlineKeyboardButton("Выполнено");
        InlineKeyboardButton subtaskButton = new InlineKeyboardButton("Создать подзадачу");

        backButton.setCallbackData(BotState.TASK_MAIN_MENU.getCommand());
        deleteButton.setCallbackData("/delete" + taskId);
        addToTimerButton.setCallbackData("/add" + taskId);
        doneButton.setCallbackData("/done" + taskId);
        subtaskButton.setCallbackData("/subtask" + taskId);

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(doneButton);
        row1.add(deleteButton);
        row1.add(addToTimerButton);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(row1);
        rowList.add(Arrays.asList(subtaskButton));
        rowList.add(Arrays.asList(backButton));

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    @Override
    public BotState getHandlerName() {
        return BotState.TASK_LIST;
    }
}
