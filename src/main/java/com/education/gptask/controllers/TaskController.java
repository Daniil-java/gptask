package com.education.gptask.controllers;

import com.education.gptask.dtos.TaskDto;
import com.education.gptask.services.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {
    private final TaskService taskService;

    @GetMapping
    public List<TaskDto> getAllTasksByUserId(@RequestParam Long userId) {
        return taskService.getTasksDtoByUserId(userId);
    }

    @GetMapping("/{taskId}")
    public TaskDto getTaskById(@PathVariable Long taskId) {
        return taskService.getTaskDtoById(taskId);
    }

    @PostMapping
    public TaskDto createTask(@Validated @RequestBody TaskDto taskDto)  {
        return taskService.createTaskByDto(taskDto);
    }

    @PutMapping
    public TaskDto updateTask(@Validated @RequestBody TaskDto taskDto) {
        return taskService.updateTask(taskDto);
    }

    @DeleteMapping("/{taskId}")
    public void deleteTaskById(@PathVariable Long taskId) {
        taskService.deleteTaskById(taskId);
    }

    @PostMapping("/subtasks/generate")
    public List<TaskDto> generateSubtasks(@Validated @RequestBody TaskDto taskDto) {
        return taskService.generateSubtasks(taskDto);
    }

    @PostMapping("/save")
    public List<TaskDto> saveAllTasks(@RequestBody List<TaskDto> dtoList) {
        return taskService.saveAllTasks(dtoList);
    }

}
