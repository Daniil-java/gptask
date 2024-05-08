package com.education.gptask.sevices;

import com.education.gptask.dtos.TaskDto;
import com.education.gptask.dtos.UserDto;
import com.education.gptask.entities.User;
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

    private final UserDto USER_DTO = new UserDto().setId(1L);
    private final TaskDto TASK_DTO = new TaskDto().setName("test").setStatus(Status.PLANNED)
            .setPriority(Priority.MUST).setComment("comment");
    private final Task TASK = new Task().setId(1L).setName("test").setUser(new User().setId(1L))
            .setStatus(Status.PLANNED).setPriority(Priority.MUST).setComment("comment");

    @Test
     void createTask() {
        Task task = taskService.createTask(TASK_DTO, USER_DTO);
        Assertions.assertNotNull(task);
        Assertions.assertEquals(USER_DTO.getId(), task.getUser().getId());
        Assertions.assertEquals(TASK_DTO.getPriority(), task.getPriority());
        Assertions.assertEquals(TASK_DTO.getStatus(), task.getStatus());
        Assertions.assertEquals(TASK_DTO.getComment(), task.getComment());
        Assertions.assertEquals(TASK_DTO.getName(), task.getName());

    }

    @Test
    void getTaskByUserId() {
        Task task = taskService.getTasksByUserId(USER_DTO.getId()).get(0);
        Assertions.assertNotNull(task);
        Assertions.assertEquals(USER_DTO.getId(), task.getUser().getId());
        Assertions.assertEquals(TASK_DTO.getPriority(), task.getPriority());
        Assertions.assertEquals(TASK_DTO.getStatus(), task.getStatus());
        Assertions.assertEquals(TASK_DTO.getComment(), task.getComment());
        Assertions.assertEquals(TASK_DTO.getName(), task.getName());
    }

    @Test
    void createSubtask() {
        Task task = taskService.createSubtask(TASK,
                new Task().setPriority(Priority.MUST).setStatus(Status.PLANNED));
        Assertions.assertNotNull(task);
        Assertions.assertEquals(TASK.getId(), task.getParent().getId());
    }

    @Test
    void createAllSubtasks() {
        List<Task> list = new ArrayList<>();
        list.add(new Task().setPriority(Priority.MUST).setStatus(Status.PLANNED));
        list.add(new Task().setPriority(Priority.MUST).setStatus(Status.PLANNED));
        List<Task> taskList = taskService.createAllSubtasks(TASK, list);

        Assertions.assertNotNull(taskList);
        for (Task task : taskList) {
            Assertions.assertEquals(TASK.getId(), task.getParent().getId());
        }
    }
}
