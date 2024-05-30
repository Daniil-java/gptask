package com.education.gptask.dtos.mappers;

import com.education.gptask.dtos.TaskDto;
import com.education.gptask.entities.task.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(ignore = true, target = "parent")
    TaskDto entityToDto(Task task);
    Task dtoToEntity(TaskDto taskDto);
    @Mapping(ignore = true, target = "parent")
    List<TaskDto> entityListToDtoList(List<Task> taskList);
    List<Task> dtoListToEntityList(List<TaskDto> taskDtoList);

}
