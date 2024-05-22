package com.education.gptask.dtos;

import com.education.gptask.entities.timer.TimerStatus;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TimerDto {
    private Long id;
    private UserDto user;
    private TimerStatus status;
    private int workDuration;
    private int shortBreakDuration;
    private int longBreakDuration;
    private int longBreakInterval;
    private boolean isAutostartWork;
    private boolean isAutostartBreak;
}
