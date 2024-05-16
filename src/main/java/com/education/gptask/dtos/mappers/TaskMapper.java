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
    @Mappings({
            @Mapping(source = "priority", target = "priority", qualifiedByName = "priorityToString"),
            @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    })
    TaskDto entityToDto(Task task);

    @Mappings({
            @Mapping(source = "priority", target = "priority", qualifiedByName = "stringToPriority"),
            @Mapping(source = "status", target = "status", qualifiedByName = "stringToStatus")
    })
    Task dtoToEntity(TaskDto taskDto);

    @Named("stringToPriority")
    static Priority stringToPriority(String priority) {
        if (priority == null) {
            return Priority.WOULD;
        }
        return Priority.valueOf(priority);
    }

    @Named("priorityToString")
    static String priorityToString(Priority priority) {
        if (priority == null) {
            return Priority.WOULD.toString();
        }
        return priority.toString();
    }

    @Named("stringToStatus")
    static Status stringToStatus(String status) {
        if (status == null) {
            return Status.PLANNED;
        }
        return Status.valueOf(status);
    }

    @Named("statusToString")
    static String statusToString(Status status) {
        if (status == null) {
            return Status.PLANNED.toString();
        }
        return status.toString();
    }
}
