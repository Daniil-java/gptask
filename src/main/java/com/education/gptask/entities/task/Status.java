package com.education.gptask.entities.task;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {
    PLANNED(0),
    IN_PROGRESS(1),
    DONE(2);

    private int code;
}
