package com.education.gptask.services;

import com.education.gptask.dtos.TaskDto;
import com.education.gptask.dtos.UserDto;
import com.education.gptask.dtos.mappers.TaskMapper;
import com.education.gptask.dtos.mappers.UserMapper;
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

    public Task createTask(TaskDto task, UserDto userDto) {
        return taskRepository.save(
                taskMapper.dtoToEntity(task).setUser(userMapper.dtoToEntity(userDto))
        );
    }

    public List<Task> getTasksByUserId(Long id) {
        return taskRepository.findTasksByUserIdAndParentIsNull(id)
                .orElseThrow(() -> new RuntimeException());
    }

    public Task createSubtask(Task parent, Task child) {
        return taskRepository.save(child.setUser(parent.getUser()).setParent(parent)
                .setUser(parent.getUser()));
    }

    public List<Task> createAllSubtasks(Task parent, List<Task> childList) {
        for (Task child : childList) {
            child.setUser(parent.getUser()).setParent(parent)
                    .setUser(parent.getUser());
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
