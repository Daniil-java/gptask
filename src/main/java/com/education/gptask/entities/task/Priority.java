package com.education.gptask.entities.task;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Priority {
    MUST(0),
    SHOULD(1),
    COULD(2),
    WOULD(3);

    private int code;
}
