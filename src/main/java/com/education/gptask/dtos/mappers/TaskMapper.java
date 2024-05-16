package com.education.gptask.dtos.mappers;

import com.education.gptask.dtos.TaskDto;
import com.education.gptask.entities.task.Priority;
import com.education.gptask.entities.task.Status;
import com.education.gptask.entities.task.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    TaskDto entityToDto(Task task);


    Task dtoToEntity(TaskDto taskDto);

}
