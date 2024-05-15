package com.education.gptask.entities.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus {
    TASK_ERROR(HttpStatus.BAD_REQUEST, "Task Error!"),
    TASK_CREATION_ERROR(HttpStatus.BAD_REQUEST, "Task Creation Error!"),
    TASK_UPDATE_ERROR(HttpStatus.BAD_REQUEST, "Task Update Error!");

    private HttpStatus httpStatus;
    private String message;
}
