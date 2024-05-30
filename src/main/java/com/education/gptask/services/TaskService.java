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
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;

    public List<TaskDto> getTasksByUserId(Long id) {
        return taskRepository.findTasksByUserIdAndParentIsNull(id)
                .map(taskMapper::entityListToDtoList)
                .orElseThrow(() -> new ErrorResponseException(ErrorStatus.TASK_ERROR));
    }

    public TaskDto getTaskDtoById(Long id) {
        return taskMapper.entityToDto(getTaskById(id));
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ErrorResponseException(ErrorStatus.TASK_ERROR)
                );
    }

    public TaskDto createTask(TaskDto taskDto) {
        if (!CollectionUtils.isEmpty(taskDto.getChildTasks())) {
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
        if (!CollectionUtils.isEmpty(taskDto.getChildTasks())) {
            throw new ErrorResponseException(ErrorStatus.TASK_UPDATE_ERROR);
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
