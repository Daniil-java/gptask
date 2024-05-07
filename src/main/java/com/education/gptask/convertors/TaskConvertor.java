package com.education.gptask.convertors;

import com.education.gptask.dtos.TaskDto;
import com.education.gptask.entities.task.Task;

public class TaskConvertor {
    private TaskConvertor() {
        throw new IllegalStateException("Utility class");
    }
    public static Task dtoToEntity(TaskDto taskDto) {
        return new Task()
                .setName(taskDto.getName())
                .setPriority(taskDto.getPriority())
                .setStatus(taskDto.getStatus())
                .setComment(taskDto.getComment());
    }
}
