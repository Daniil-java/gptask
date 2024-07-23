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
public class TaskHandler implements MessageHandler {
    private final TaskService taskService;
    private final TelegramBot telegramBot;
    private final UserService userService;

    /*
        Данная константа является стандартным значением
        строк в списке задач.
    */
    private final static int LIST_PAGE_ROW_COUNT = 8;

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
                telegramBot.sendMessage(new DeleteMessage(
                        String.valueOf(chatId),
                        userEntity.getLastUpdatedTaskMessageId().intValue()
                ));
            }
            telegramBot.sendMessage(new DeleteMessage(String.valueOf(chatId), messageId));
            List<Task> taskList = taskService.getParentDoneTasksByUserId(
                            userEntity.getId(),
                            PageRequest.of(0, LIST_PAGE_ROW_COUNT, Sort.by("id"))
            );
            Message futureMessage = telegramBot
                    .sendReturnedMessage(getList(taskList, chatId, 0, false));
            userEntity.setLastUpdatedTaskMessageId(Long.valueOf(futureMessage.getMessageId()));
            userService.updateUserEntity(userEntity);
            return null;
        }

        if (botState.equals(BotState.TASK_MAIN_MENU)) {
            EditMessageText editMessageText;
            List<Task> taskList;
            int page = 0;
            if (userAnswer.startsWith("/next") || userAnswer.startsWith("/prev")) {
                page = userAnswer.startsWith("/next")
                        ? Integer.parseInt(userAnswer.substring("/next".length()))
                        : Integer.parseInt(userAnswer.substring("/prev".length()));
            }
            taskList = taskService.getParentDoneTasksByUserId(
                    userEntity.getId(),
                    PageRequest.of(page, LIST_PAGE_ROW_COUNT, Sort.by("id")));

            editMessageText = MessageTypeConverter
                    .convertSendToEdit(getList(taskList, chatId, page, false));
            editMessageText.setMessageId(userEntity.getLastUpdatedTaskMessageId().intValue());
            userEntity.setBotState(BotState.TASK_LIST);
            userService.updateUserEntity(userEntity);
            return editMessageText;
        }

        if (botState.equals(BotState.TASK_MAIN_MENU_CLOSE)) {
            if (userEntity.getLastUpdatedTaskMessageId() != null) {
                return new DeleteMessage(String.valueOf(chatId), userEntity.getLastUpdatedTaskMessageId().intValue());
            }
        }

        return replyMessage;
    }

    /*
        Метод getList(...) отвечает за предоставление информации
        о задачах, ввиде клавиатуры, в Телеграм.
    */
    public static SendMessage getList
            (List<Task> taskList, long chatId, int page, boolean isSub) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        if (taskList.isEmpty()) {
            sendMessage.setText("You don't have any task");
            sendMessage.setReplyMarkup(getInlineMessageButtons());
        } else {
            sendMessage.setText("\uD83D\uDCCB Выберите задачу:");
            sendMessage.setReplyMarkup(
                    getInlineMessageListTaskButtons(taskList, LIST_PAGE_ROW_COUNT, page, isSub));
        }
        return sendMessage;
    }
    public static InlineKeyboardMarkup getInlineMessageListTaskButtons(
            List<Task> taskList, int rowCount, int page, boolean isSub
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
        String suffix = isSub ? "sub" + taskList.get(0).getParent().getId() + "#" : "";
        if (page != 0) {
            InlineKeyboardButton prevButton = new InlineKeyboardButton("⬅️");
            prevButton.setCallbackData("/prev" + suffix + (page - 1));
            row.add(prevButton);
        }
        if (taskList.size() == rowCount) {
            InlineKeyboardButton nextButton = new InlineKeyboardButton("➡️");
            nextButton.setCallbackData("/next" + suffix + (page + 1));
            row.add(nextButton);
        }

        rowList.add(row);

        if (!isSub) {
            InlineKeyboardButton createButton = new InlineKeyboardButton("Создать");
            createButton.setCallbackData(BotState.TASK_CREATE.getCommand());
            rowList.add(Arrays.asList(createButton));
        } else {
            InlineKeyboardButton backButton = new InlineKeyboardButton("Назад");
            backButton.setCallbackData("/id" + taskList.get(0).getParent().getId());
            rowList.add(Arrays.asList(backButton));
        }

        InlineKeyboardButton closeButton = new InlineKeyboardButton("Закрыть");
        closeButton.setCallbackData(BotState.TASK_MAIN_MENU_CLOSE.getCommand());
        rowList.add(Arrays.asList(closeButton));

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
        return Arrays.asList(BotState.TASK, BotState.TASK_MAIN_MENU, BotState.TASK_MAIN_MENU_CLOSE);
    }
}
