package com.education.gptask.services;

import com.education.gptask.dtos.TaskDto;
import com.education.gptask.dtos.mappers.TaskMapper;
import com.education.gptask.dtos.mappers.UserMapper;
import com.education.gptask.entities.error.ErrorResponseException;
import com.education.gptask.entities.error.ErrorStatus;
import com.education.gptask.entities.task.Task;
import com.education.gptask.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;

    public Task createTask(TaskDto task) {
        return taskRepository.save(
                taskMapper.dtoToEntity(task)
        );
    }

    public List<Task> getTasksByUserId(Long id) {
        return taskRepository.findTasksByUserIdAndParentIsNull(id)
                .orElseThrow(() -> new ErrorResponseException(ErrorStatus.TASK_ERROR));
    }
    public Task createSubtask(Task parent, Task child) {
        return taskRepository.save(
                child.setUser(parent.getUser()).setParent(parent));
    }

    public List<Task> createAllSubtasks(List<Task> childList) {
        return taskRepository.saveAll(childList);
    }
    public List<Task> createAllSubtasks(Task parent, List<Task> childList) {
        for (Task child : childList) {
            child.setUser(parent.getUser()).setParent(parent);
        }
        return taskRepository.saveAll(childList);
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
