package com.education.gptask.entities.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus {
    TASK_ERROR(HttpStatus.BAD_REQUEST, "Task Error!"),
    TASK_CREATION_ERROR(HttpStatus.BAD_REQUEST, "Task Creation Error!"),
    TASK_UPDATE_ERROR(HttpStatus.BAD_REQUEST, "Task Update Error!"),
    TASK_SUBTASK_GENERATION_ERROR(HttpStatus.BAD_REQUEST, "Subtask Generation Error!"),
    TIMER_ERROR(HttpStatus.BAD_REQUEST, "Timer Error!"),
    TIMER_CREATION_ERROR(HttpStatus.BAD_REQUEST, "Timer Create Error!"),
    TIMER_UPDATE_ERROR(HttpStatus.BAD_REQUEST, "Timer Update Error!"),
    TIMER_TASK_ERROR(HttpStatus.BAD_REQUEST, "Timer Task Error! Check your task id!"),
    USER_CREATION_ERROR(HttpStatus.BAD_REQUEST, "User Creation Error!"),
    USER_NOT_FOUND_ERROR(HttpStatus.BAD_REQUEST, "User not found!");

    private HttpStatus httpStatus;
    private String message;
}
