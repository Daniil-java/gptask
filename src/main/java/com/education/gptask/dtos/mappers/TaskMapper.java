package com.education.gptask.dtos.mappers;

import com.education.gptask.dtos.TaskDto;
import com.education.gptask.entities.task.Task;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    TaskDto entityToDto(Task task);
    Task dtoToEntity(TaskDto taskDto);
}
