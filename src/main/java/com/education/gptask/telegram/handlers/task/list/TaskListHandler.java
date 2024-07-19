package com.education.gptask.telegram.handlers.task.list;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.entities.task.Priority;
import com.education.gptask.entities.task.Status;
import com.education.gptask.entities.task.Task;
import com.education.gptask.services.TaskService;
import com.education.gptask.services.TimerService;
import com.education.gptask.services.UserService;
import com.education.gptask.telegram.TelegramBot;
import com.education.gptask.telegram.entities.BotState;
import com.education.gptask.telegram.handlers.MessageHandler;
import com.education.gptask.telegram.handlers.task.TaskHandler;
import com.education.gptask.telegram.handlers.task.creation.TaskCreationHandler;
import com.education.gptask.telegram.utils.builders.BotApiMethodBuilder;
import com.education.gptask.telegram.utils.converters.MessageTypeConverter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
@Slf4j
public class TaskListHandler implements MessageHandler {
    private final UserService userService;
    private final TelegramBot telegramBot;
    private final TaskService taskService;
    private final TimerService timerService;
    private final TaskHandler taskHandler;

    @Override
    public BotApiMethod handle(Message message, UserEntity userEntity) {
        Long chatId = message.getChatId();
        int messageId = message.getMessageId();
        String userAnswer = message.getText();
        BotState botState = userEntity.getBotState();
        SendMessage replyMessage = new SendMessage(String.valueOf(chatId),
                "Что-то пошло не так ¯\\_(ツ)_/¯");

        if (botState.equals(BotState.TASK_LIST)) {
            if (!userAnswer.isEmpty() && userAnswer.startsWith("/id")) {
                long taskId;
                try {
                    taskId = Long.parseLong(userAnswer.substring("/id".length()));
                } catch (NumberFormatException e) {
                    return replyMessage;
                }
                Task task = taskService.getTaskById(taskId);
                EditMessageText editMessageText = BotApiMethodBuilder.makeEditMessageText(
                        chatId,
                        Math.toIntExact(userEntity.getLastUpdatedTaskMessageId()),
                        getTaskInfo(task)
                );
                editMessageText.setReplyMarkup(getInlineMessageButtons(taskId));
                return editMessageText;
            }


            if (!userAnswer.isEmpty() && userAnswer.startsWith("/delete")) {
                long taskId = Long.parseLong(userAnswer.substring("/delete".length()));
                taskService.deleteTaskById(taskId);
                userEntity.setBotState(BotState.TASK_MAIN_MENU);
                return taskHandler.handle(message, userEntity);
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
            if (!userAnswer.isEmpty() && checkPriority(userAnswer)) {
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

                return BotApiMethodBuilder
                        .makeEditMessageText(
                                chatId,
                                Math.toIntExact(userEntity.getLastUpdatedTaskMessageId()),
                                "Give a name#comment"
                        );
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
            userEntity.setBotState(BotState.TASK_MAIN_MENU);
            return taskHandler.handle(message, userEntity);
        }

        if (!userAnswer.isEmpty() && userAnswer.startsWith("/generate")) {
            long taskId = Long.parseLong(userAnswer.substring("/generate".length()));
            List<Task> taskList = taskService.generateSubtasksById(taskId);
            EditMessageText editMessageText = BotApiMethodBuilder
                    .makeEditMessageText(
                            chatId,
                            Math.toIntExact(userEntity.getLastUpdatedTaskMessageId()),
                            getTaskListInfo(taskList)
                    );
            editMessageText.setReplyMarkup(getInlineMessageAcceptButtons(taskId, taskList));
            return editMessageText;
        }

        if (!userAnswer.isEmpty() &&
                (userAnswer.startsWith("/accept") || userAnswer.startsWith("/reject"))) {
            long taskId;
            if (userAnswer.startsWith("/accept")) {
                taskId = Long.parseLong(userAnswer.substring("/accept".length()));
                message.setText("/id" + taskId);
                handle(message, userEntity);
            } else {
                taskId = Long.parseLong(
                        userAnswer.substring("/reject".length(), userAnswer.indexOf("#"))
                );
                String[] subIds = userAnswer
                        .substring(userAnswer.indexOf("#") + 1).split("#");
                List<Long> substackIds = new ArrayList<>();
                for (int i = 0; i < subIds.length; i++) {
                    substackIds.add(Long.parseLong(subIds[i]));
                }
                taskService.deleteAllById(substackIds);
            }
            message.setText("/id" + taskId);
            return handle(message, userEntity);
        }
        if (!userAnswer.isEmpty() && userAnswer.startsWith("/getsubs")) {
            long taskId = Long.parseLong(userAnswer.substring("/getsubs".length()));
            List<Task> taskList = taskService.getChildTasksByTaskId(taskId,
                    PageRequest.of(0, 8, Sort.by("id")));
            EditMessageText editMessageText = MessageTypeConverter
                    .convertSendToEdit(TaskHandler.getList(taskList, chatId, 0, true));
            editMessageText.setMessageId(Math.toIntExact(userEntity.getLastUpdatedTaskMessageId()));
            return editMessageText;
        }
        if (!userAnswer.isEmpty() && (userAnswer.startsWith("/nextsub") || userAnswer.startsWith("/prevsub"))) {
            String command = userAnswer.startsWith("/nextsub")
                    ? "/nextsub"
                    : "/prevsub";
            long id = Long.parseLong(userAnswer.substring(command.length(), userAnswer.indexOf("#")));
            int page = Integer.parseInt(userAnswer.substring(userAnswer.indexOf("#") + 1));
            List<Task> taskList = taskService.getChildTasksByTaskId(
                    id,
                    PageRequest.of(page, 8, Sort.by("id")));

            EditMessageText editMessageText = MessageTypeConverter
                    .convertSendToEdit(TaskHandler.getList(taskList, chatId, page, true));
            editMessageText.setMessageId(Math.toIntExact(userEntity.getLastUpdatedTaskMessageId()));
            return editMessageText;
        }
        if (!userAnswer.isEmpty() && (userAnswer.startsWith("/next") || userAnswer.startsWith("/prev") ||
                userAnswer.startsWith("/subnext") || userAnswer.startsWith("/subnext"))) {
            userEntity.setBotState(BotState.TASK_MAIN_MENU);
            return taskHandler.handle(message, userEntity);
        }
        return replyMessage;
    }

    private String getTaskInfo(Task task) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(String.format("[%s] %s\n", task.getId(), task.getName()))
                .append(task.getComment())
                .append("\n\n");

        for (Task t: task.getChildTasks()) {
            stringBuilder
                    .append(String.format("[%s] %s\n", t.getId(), t.getName()))
                    .append(t.getComment())
                    .append("\n");

        }

        return stringBuilder.toString();
    }

    private String getTaskListInfo(List<Task> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Task task: list) {
            stringBuilder
                    .append(task.getName() + "\n")
                    .append(task.getStatus() + "\n")
                    .append(task.getComment() + "\n\n");
        }
        return stringBuilder.toString();
    }
    private boolean checkPriority(String str) {
        return str.startsWith(Priority.MUST.name()) || str.startsWith(Priority.COULD.name()) ||
                str.startsWith(Priority.WOULD.name()) || str.startsWith(Priority.SHOULD.name());
    }

    public static InlineKeyboardMarkup getInlineMessageAcceptButtons(long taskId, List<Task> list) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        InlineKeyboardButton acceptButton = new InlineKeyboardButton("Принять");
        InlineKeyboardButton rejectButton = new InlineKeyboardButton("Отклонить");
        StringBuilder stringBuilder = new StringBuilder();
        for (Task task: list) {
            stringBuilder.append("#" + task.getId());
        }

        acceptButton.setCallbackData("/accept" + taskId);
        rejectButton.setCallbackData("/reject" + taskId + stringBuilder.toString());

        rowList.add(Arrays.asList(acceptButton, rejectButton));

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup getInlineMessageButtons(long taskId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton backButton = new InlineKeyboardButton("Назад");
        InlineKeyboardButton deleteButton = new InlineKeyboardButton("Удалить");
        InlineKeyboardButton addToTimerButton = new InlineKeyboardButton("Добавить в таймер");
        InlineKeyboardButton doneButton = new InlineKeyboardButton("Выполнено");
        InlineKeyboardButton subtaskButton = new InlineKeyboardButton("Создать подзадачу");
        InlineKeyboardButton subtaskGenerationButton = new InlineKeyboardButton("AI генерация подзадач");
        InlineKeyboardButton getSubtasksButton = new InlineKeyboardButton("Подзадачи");

        backButton.setCallbackData(BotState.TASK_MAIN_MENU.getCommand());
        deleteButton.setCallbackData("/delete" + taskId);
        addToTimerButton.setCallbackData("/add" + taskId);
        doneButton.setCallbackData("/done" + taskId);
        subtaskButton.setCallbackData("/subtask" + taskId);
        subtaskGenerationButton.setCallbackData("/generate" + taskId);
        getSubtasksButton.setCallbackData("/getsubs" + taskId);

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(doneButton);
        row1.add(deleteButton);
        row1.add(addToTimerButton);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(row1);
        rowList.add(Arrays.asList(getSubtasksButton, subtaskButton, subtaskGenerationButton));
        rowList.add(Arrays.asList(backButton));

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    @Override
    public List<BotState> getHandlerListName() {
        return Arrays.asList(BotState.TASK_LIST, BotState.TASK_CREATE_SUBTASK);
    }
}
