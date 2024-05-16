package com.education.gptask.dtos;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class TaskDto {
    private Long id;
    private String name;
    private String priority;
    private String status;
    private String comment;
    private UserDto user;
    private TaskDto parent;
    private List<TaskDto> childTasks;
}
