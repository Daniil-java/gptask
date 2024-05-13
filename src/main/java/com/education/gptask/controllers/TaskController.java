package com.education.gptask.controllers;

import com.education.gptask.dtos.TaskDto;
import com.education.gptask.entities.task.Task;
import com.education.gptask.services.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/task")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping("/")
    public List<Task> getAllTasksByUserId(@RequestParam Long userId) {
        return taskService.getTasksByUserId(userId);
    }

    @PostMapping("/create")
    public Task createTask(@RequestBody TaskDto taskDto) {
        return taskService.createTask(taskDto);
    }

    @PostMapping("/create/sub}")
    public List<Task> createAllSubtask(@RequestBody List<Task> taskList) {
        return taskService.createAllSubtasks(taskList);
    }
}
