package com.education.gptask.entities.task;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Priority {
    MUST,
    SHOULD,
    COULD,
    WOULD;
}
