package com.education.gptask.dtos;

import com.education.gptask.entities.task.Priority;
import com.education.gptask.entities.task.Status;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class TaskDto {
    private Long id;
    private String name;
    private Priority priority;
    private Status status;
    private String comment;
    private UserDto user;
    private TaskDto parent;
    private List<TaskDto> childTasks;
}
