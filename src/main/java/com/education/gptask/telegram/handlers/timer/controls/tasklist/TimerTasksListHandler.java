package com.education.gptask.telegram.handlers.timer.controls.tasklist;

import com.education.gptask.entities.UserEntity;
import com.education.gptask.entities.task.Status;
import com.education.gptask.entities.task.Task;
import com.education.gptask.entities.timer.Timer;
import com.education.gptask.services.TaskService;
import com.education.gptask.services.TimerService;
import com.education.gptask.services.UserService;
import com.education.gptask.telegram.entities.BotState;
import com.education.gptask.telegram.handlers.MessageHandler;
import com.education.gptask.telegram.handlers.timer.TimerHandler;
import com.education.gptask.telegram.utils.builders.BotApiMethodBuilder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@AllArgsConstructor
public class TimerTasksListHandler implements MessageHandler {
    private final TimerService timerService;
    private final UserService userService;
    private final TaskService taskService;
    @Override
    public BotApiMethod handle(Message message, UserEntity userEntity) {
        String userAnswer = message.getText();
        BotState botState = userEntity.getBotState();
        Timer timer = timerService.getTimersByUserId(userEntity.getId()).get(0);
        List<Task> taskList = null;
        if (!timer.getTasks().isEmpty()) {
            taskList = taskService.getTasksByTimerId(timer.getId());
        }
        EditMessageText editMessageText = BotApiMethodBuilder.makeEditMessageText(
                message.getChatId(),
                timer.getTelegramMessageId(),
                TimerHandler.getTimerInfo(timer, botState.toString(), taskList)
        );

        if (botState.equals(BotState.TIMER_TASKS_LIST)) {
            editMessageText.setReplyMarkup(getInlineMessageButtons());
        }

        if (botState.equals(BotState.TIMER_TASKS_LIST_DELETE)) {
            if (!userAnswer.isEmpty() && userAnswer.startsWith("/delete")) {
                taskService.deleteTaskById((Long.valueOf(userAnswer.substring("/delete".length()))));
                userEntity.setBotState(BotState.TIMER_TASKS_LIST);
                userService.updateUserEntity(userEntity);
                return handle(message, userEntity);
            } else {
                editMessageText.setReplyMarkup(getInlineMessageUnbindButtons(timer));
            }
        }

        if (botState.equals(BotState.TIMER_TASKS_LIST_DONE)) {
            if (!userAnswer.isEmpty() && userAnswer.startsWith("/done")) {
                Task task = taskService.getTaskById(Long.valueOf(userAnswer.substring("/done".length())));
                task.setStatus(Status.DONE);
                taskService.createTask(task);
                userEntity.setBotState(BotState.TIMER_TASKS_LIST);
                userService.updateUserEntity(userEntity);
                return handle(message, userEntity);
            } else {
                editMessageText.setReplyMarkup(getInlineMessageDoneButtons(timer));
            }

        }
        return editMessageText;
    }

    public static InlineKeyboardMarkup getInlineMessageButtons() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton unbindButton = new InlineKeyboardButton("Отвязать задачу");
        InlineKeyboardButton doneButton = new InlineKeyboardButton("Отметить выполненым");

        unbindButton.setCallbackData(BotState.TIMER_TASKS_LIST_DELETE.getCommand());
        doneButton.setCallbackData(BotState.TIMER_TASKS_LIST_DONE.getCommand());

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(Arrays.asList(unbindButton));
        rowList.add(Arrays.asList(doneButton));

        InlineKeyboardButton button = new InlineKeyboardButton("Назад");
        button.setCallbackData(BotState.TIMER_STATUS.getCommand());
        rowList.add(Arrays.asList(button));

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup getInlineMessageUnbindButtons(Timer timer) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        for (Task task: timer.getTasks()) {
            InlineKeyboardButton button = new InlineKeyboardButton(
                    String.format("[%s] %s", task.getPriority(), task.getName())
            );
            button.setCallbackData("/delete" + task.getId());
            rowList.add(Arrays.asList(button));
        }

        InlineKeyboardButton button = new InlineKeyboardButton("Назад");
        button.setCallbackData(BotState.TIMER_TASKS_LIST.getCommand());
        rowList.add(Arrays.asList(button));

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup getInlineMessageDoneButtons(Timer timer) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        for (Task task: timer.getTasks()) {
            if (task.getStatus().equals(Status.DONE)) continue;
            InlineKeyboardButton button = new InlineKeyboardButton(task.getId().toString());
            button.setCallbackData("/done" + task.getId());
            rowList.add(Arrays.asList(button));
        }

        InlineKeyboardButton button = new InlineKeyboardButton("Назад");
        button.setCallbackData(BotState.TIMER_TASKS_LIST.getCommand());
        rowList.add(Arrays.asList(button));

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    @Override
    public List<BotState> getHandlerListName() {
        return Arrays.asList(
                BotState.TIMER_TASKS_LIST, BotState.TIMER_TASKS_LIST_DONE, BotState.TIMER_TASKS_LIST_DELETE
        );
    }
}
