package com.education.gptask.mappers;

import com.education.gptask.dtos.TaskDto;
import com.education.gptask.dtos.UserDto;
import com.education.gptask.dtos.mappers.TaskMapper;
import com.education.gptask.entities.User;
import com.education.gptask.entities.task.Priority;
import com.education.gptask.entities.task.Status;
import com.education.gptask.entities.task.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class TaskMapperTest {
    @Autowired
    private TaskMapper taskMapper;
    private final UserDto USER_DTO = new UserDto().setId(1L);
    private final User USER = new User().setId(1L);
    private final TaskDto TASK_DTO_1 =
            new TaskDto()
                    .setPriority(0)
                    .setStatus(1)
                    .setUser(USER_DTO)
            ;

    private final TaskDto TASK_DTO_2 =
            new TaskDto()
                    .setPriority(0)
                    .setStatus(1)
                    .setUser(USER_DTO)
            ;
    private final List<TaskDto> taskDtoList = new ArrayList<>();

    private final Task TASK_1 =
            new Task()
                    .setPriority(Priority.MUST)
                    .setStatus(Status.IN_PROGRESS)
                    .setUser(USER)
            ;
    private final Task TASK_2 =
            new Task()
                    .setPriority(Priority.MUST)
                    .setStatus(Status.IN_PROGRESS)
                    .setUser(USER)
            ;
    private final List<Task> taskList = new ArrayList<>();

    @BeforeEach
    void setUp() {
        taskDtoList.add(TASK_DTO_1);
        taskDtoList.add(TASK_DTO_2);

        taskList.add(TASK_1);
        taskList.add(TASK_2);
    }

    @Test
    void dtoToEntity() {
        TaskDto taskDto = new TaskDto()
                .setName("name")
                .setPriority(0)
                .setStatus(1)
                .setUser(USER_DTO)
                .setParent(new TaskDto().setId(1L))
                .setChildTasks(taskDtoList);
        Task task = taskMapper.dtoToEntity(taskDto);

        Assertions.assertNotNull(task);
        Assertions.assertEquals(taskDto.getName(), task.getName());
        Assertions.assertEquals(taskDto.getPriority(), task.getPriority().getCode());
        Assertions.assertEquals(taskDto.getStatus(), task.getStatus().getCode());
        Assertions.assertEquals(taskDto.getUser().getId(), task.getUser().getId());
        Assertions.assertEquals(task.getChildTasks().size(), 2);
        Assertions.assertEquals(task.getChildTasks().get(0).getPriority().getCode(), taskDtoList.get(0).getPriority());
    }

    @Test
    void entityToDto() {
        Task task = new Task()
                .setName("name")
                .setUser(USER)
                .setPriority(Priority.MUST)
                .setStatus(Status.IN_PROGRESS)
                .setChildTasks(taskList);
        TaskDto taskDto = taskMapper.entityToDto(task);

        Assertions.assertNotNull(task);
        Assertions.assertEquals(taskDto.getName(), task.getName());
        Assertions.assertEquals(taskDto.getPriority(), Priority.MUST.getCode());
        Assertions.assertEquals(taskDto.getStatus(), Status.IN_PROGRESS.getCode());
        Assertions.assertEquals(taskDto.getUser().getId(), task.getUser().getId());
        Assertions.assertEquals(taskDto.getChildTasks().size(), 2);
        Assertions.assertEquals(taskDto.getChildTasks().get(0).getPriority(), taskList.get(0).getPriority().getCode() );
    }
}
