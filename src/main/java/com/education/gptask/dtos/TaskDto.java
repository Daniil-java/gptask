package com.education.gptask.dtos;

import com.education.gptask.entities.task.Priority;
import com.education.gptask.entities.task.Status;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TaskDto {
    private String name;

    private Priority priority;

    private Status status;

    private String comment;
}
