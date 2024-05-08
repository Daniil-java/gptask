package com.education.gptask.mappers;

import com.education.gptask.dtos.TaskDto;
import com.education.gptask.dtos.mappers.TaskMapper;
import com.education.gptask.entities.task.Priority;
import com.education.gptask.entities.task.Status;
import com.education.gptask.entities.task.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TaskMapperTest {
    @Autowired
    private TaskMapper taskMapper;

    @Test
    void dtoToEntity() {
        TaskDto taskDto = new TaskDto().setName("name").setPriority(Priority.MUST).setStatus(Status.IN_PROGRESS);
        Task task = taskMapper.dtoToEntity(taskDto);

        Assertions.assertNotNull(task);
        Assertions.assertEquals(taskDto.getName(), task.getName());
        Assertions.assertEquals(taskDto.getPriority(), task.getPriority());
        Assertions.assertEquals(taskDto.getStatus(), task.getStatus());
    }

    @Test
    void entityToDto() {
        Task task = new Task().setName("name").setPriority(Priority.MUST).setStatus(Status.IN_PROGRESS);
        TaskDto taskDto = taskMapper.entityToDto(task);

        Assertions.assertNotNull(task);
        Assertions.assertEquals(taskDto.getName(), task.getName());
        Assertions.assertEquals(taskDto.getPriority(), task.getPriority());
        Assertions.assertEquals(taskDto.getStatus(), task.getStatus());
    }
}
