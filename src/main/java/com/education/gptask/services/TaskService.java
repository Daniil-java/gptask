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

    public List<Task> getTasksByUserId(Long id) {
        return taskRepository.findTasksByUserIdAndParentIsNull(id)
                .orElseThrow(() -> new ErrorResponseException(ErrorStatus.TASK_ERROR));
    }

    public TaskDto getTaskById(Long id) {
        return taskMapper.entityToDto(
                taskRepository.getReferenceById(id)
        );
    }

    public TaskDto createTask(TaskDto taskDto) {
        if (!taskDto.getChildTasks().isEmpty()) {
            throw new ErrorResponseException(ErrorStatus.TASK_CREATION_ERROR);
        }
        return taskMapper.entityToDto(
                taskRepository.save(
                        taskMapper.dtoToEntity(taskDto)
        ));
    }

    public TaskDto updateTask(TaskDto taskDto) {
        if (taskDto.getId() == null) {
            throw new ErrorResponseException(ErrorStatus.TASK_UPDATE_ERROR);
        }
        if (!taskDto.getChildTasks().isEmpty()) {
            taskDto.getChildTasks().clear();
        }
        return taskMapper.entityToDto(
                taskRepository.save(
                        taskMapper.dtoToEntity(taskDto)
                ));
    }

    public void deleteTaskById(Long id) {
        taskRepository.deleteById(id);
    }
}
