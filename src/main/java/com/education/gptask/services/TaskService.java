package com.education.gptask.services;

import com.education.gptask.dtos.TaskDto;
import com.education.gptask.dtos.mappers.TaskMapper;
import com.education.gptask.entities.task.Task;
import com.education.gptask.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public Task createTask(TaskDto task) {
        return taskRepository.save(taskMapper.dtoToEntity(task));
    }

    public Task getTaskById(Long id) {
        return taskRepository.getReferenceById(id);
    }

    public Task updateTask(Task task) {
        return taskRepository.save(task);
    }

    public void deleteTaskById(Long id) {
        taskRepository.deleteById(id);
    }
}
