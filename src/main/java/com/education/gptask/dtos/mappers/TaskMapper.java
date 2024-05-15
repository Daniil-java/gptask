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
            @Mapping(source = "priority", target = "priority", qualifiedByName = "priorityToInt"),
            @Mapping(source = "status", target = "status", qualifiedByName = "statusToInt")
    })
    TaskDto entityToDto(Task task);

    @Mappings({
            @Mapping(source = "priority", target = "priority", qualifiedByName = "intToPriority"),
            @Mapping(source = "status", target = "status", qualifiedByName = "intToStatus")
    })
    Task dtoToEntity(TaskDto taskDto);

    @Named("intToPriority")
    static Priority longToPriority(int priority) {
        if (priority == 0) {
            return Priority.MUST;
        } else if (priority == 1) {
            return Priority.SHOULD;
        } else if (priority == 2) {
            return Priority.COULD;
        } else if (priority == 3) {
            return Priority.WOULD;
        }
        return null;
    }

    @Named("priorityToInt")
    static int priorityToInt(Priority priority) {
        switch (priority) {
            case MUST: return 0;
            case SHOULD: return 1;
            case COULD: return 2;
            case WOULD: return 3;
        }
        return 3;
    }

    @Named("intToStatus")
    static Status intToStatus(int status) {
        if (status == 0) {
            return Status.PLANNED;
        } else if (status == 1) {
            return Status.IN_PROGRESS;
        } else if (status == 2) {
            return Status.DONE;
        }
        return null;
    }

    @Named("statusToInt")
    static int statusToInt(Status status) {
        switch (status) {
            case PLANNED: return 0;
            case IN_PROGRESS: return 1;
            case DONE: return 2;
        }
        return 0;
    }
}
