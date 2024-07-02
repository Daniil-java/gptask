package com.education.gptask.telegram;

import com.education.gptask.telegram.facades.MessageFacade;
import com.education.gptask.telegram.handlers.processors.TimerScheduleProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    @Value("${bot.name}")
    private String botName;

    public TelegramBot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }
    @Autowired
    private MessageFacade messageFacade;
    @Autowired
    private TimerScheduleProcessor timerScheduleProcessor;

    @Override
    public void onUpdateReceived(Update update) {
        sendMessage(messageFacade.handleUpdate(update));
    }

    public void sendMessage(BotApiMethod sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public Message sendReturnedMessage(SendMessage sendMessage) {
        try {
            return execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Scheduled(cron = "0 * * * * *")
    public void checkTimesTimeStatus() {
        log.info("Schedule timer checker is starting!");
        List<BotApiMethod> messages = timerScheduleProcessor.checkTimesTimeStatus();
        if (messages != null && !messages.isEmpty()) {
            log.info("Expired timer list size: " + messages.size());
            for (BotApiMethod message: messages) {
                try {
                    Thread.sleep(1000);
                    sendMessage(message);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            log.info("Expired timer list is empty");
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }
}
