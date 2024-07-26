package com.education.gptask.telegram.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadUtil {
    public static void sleep(int milliseconds, String logError) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            log.error(logError);
        }
    }
}
