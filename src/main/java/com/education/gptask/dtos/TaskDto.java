package com.education.gptask.dtos;

import com.education.gptask.entities.task.Priority;
import com.education.gptask.entities.task.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class TaskDto {
    private Long id;
    private String name;
    @NotNull(message = "Priority can't be empty!")
    private Priority priority;
    @NotNull(message = "Status can't be empty!")
    private Status status;
    private String comment;
    private UserDto user;
    private List<TaskDto> childTasks;
}
