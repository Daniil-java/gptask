package com.education.gptask.entities.task;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {
    IN_PROGRESS(0),
    DONE(1);

    private int code;
}
