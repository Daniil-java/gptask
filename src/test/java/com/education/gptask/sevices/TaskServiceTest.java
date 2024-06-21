package com.education.gptask.sevices;

import com.education.gptask.dtos.TaskDto;
import com.education.gptask.dtos.UserDto;
import com.education.gptask.entities.UserEntity;
import com.education.gptask.entities.task.Priority;
import com.education.gptask.entities.task.Status;
import com.education.gptask.entities.task.Task;
import com.education.gptask.repositories.TaskRepository;
import com.education.gptask.services.TaskService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class TaskServiceTest {
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskRepository taskRepository;

    private final UserDto USER_DTO = new UserDto().setId(10001L);
    private final UserEntity USEREntity = new UserEntity().setId(10001L);
    private final TaskDto TASK_DTO_1 =
            new TaskDto()
                    .setPriority(Priority.MUST)
                    .setStatus(Status.DONE)
                    .setUser(USER_DTO)
            ;

    private final TaskDto TASK_DTO_2 =
            new TaskDto()
                    .setPriority(Priority.MUST)
                    .setStatus(Status.DONE)
                    .setUser(USER_DTO)
            ;
    private final List<TaskDto> taskDtoList = new ArrayList<>();

    private final Task TASK_1 =
            new Task()
                    .setPriority(Priority.MUST)
                    .setStatus(Status.IN_PROGRESS)
                    .setUserEntity(USEREntity)
            ;
    private final Task TASK_2 =
            new Task()
                    .setPriority(Priority.MUST)
                    .setStatus(Status.IN_PROGRESS)
                    .setUserEntity(USEREntity)
            ;
    private final List<Task> taskList = new ArrayList<>();

    @BeforeEach
    void setUp() {
        taskDtoList.add(TASK_DTO_1);
        taskDtoList.add(TASK_DTO_2);

        taskList.add(TASK_1);
        taskList.add(TASK_2);
    }
}
