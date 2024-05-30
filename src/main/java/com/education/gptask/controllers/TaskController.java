package com.education.gptask.controllers;

import com.education.gptask.dtos.TaskDto;
import com.education.gptask.services.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping
    public List<TaskDto> getAllTasksByUserId(@RequestParam Long userId) {
        return taskService.getTasksByUserId(userId);
    }

    @GetMapping("/{taskId}")
    public TaskDto getTaskById(@PathVariable Long taskId) {
        return taskService.getTaskDtoById(taskId);
    }

    @PostMapping
    public TaskDto createTask(@Validated @RequestBody TaskDto taskDto)  {
        return taskService.createTask(taskDto);
    }

    @PutMapping
    public TaskDto updateTask(@Validated @RequestBody TaskDto taskDto) {
        return taskService.updateTask(taskDto);
    }

    @DeleteMapping("/{taskId}")
    public void deleteTaskById(@PathVariable Long taskId) {
        taskService.deleteTaskById(taskId);
    }


}
