package com.education.gptask.services;

import com.education.gptask.dtos.TaskDto;
import com.education.gptask.dtos.mappers.TaskMapper;
import com.education.gptask.dtos.mappers.UserMapper;
import com.education.gptask.entities.error.ErrorResponseException;
import com.education.gptask.entities.error.ErrorStatus;
import com.education.gptask.entities.task.Status;
import com.education.gptask.entities.task.Task;
import com.education.gptask.repositories.TaskRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
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
    private final OpenAiApiFeignService openAiApiFeignService;

    public List<TaskDto> getTasksDtoByUserId(Long id) {
        return taskMapper.entityListToDtoList(getTasksByUserId(id));
    }

    public List<Task> getTasksByUserId(Long id) {
        return taskRepository.findTasksByUserEntityIdAndParentIsNull(id)
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

    public TaskDto createTaskByDto(TaskDto taskDto) {
        return taskMapper.entityToDto(
                createTask(
                        taskMapper.dtoToEntity(taskDto)
        ));
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
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

    public void deleteAllById(Iterable<? extends Long> ids) {
        taskRepository.deleteAllById(ids);
    }

    @Transactional
    public List<TaskDto> generateSubtasks(TaskDto taskDto) {
        try {
            List<Task> taskList =
                    taskMapper.dtoListToEntityList(
                            openAiApiFeignService.generateSubtasks(taskDto.getName(), taskDto.getComment())
                    );

            for (Task task: taskList) {
                task.setStatus(Status.PLANNED);
                task.setUserEntity(userMapper.dtoToEntity(taskDto.getUser()));
                task.setParent(new Task().setId(taskDto.getId()));
            }
            return taskMapper.entityListToDtoList(taskRepository.saveAll(taskList));
        } catch (JsonProcessingException e) {
            throw new ErrorResponseException(ErrorStatus.TASK_SUBTASK_GENERATION_ERROR);
        }
    }

    @Transactional
    public List<Task> generateSubtasksById(Long taskId) {
        try {
            Task task = getTaskById(taskId);
            List<Task> taskList =
                    taskMapper.dtoListToEntityList(
                            openAiApiFeignService.generateSubtasks(task.getName(), task.getComment())
                    );

            for (Task taskObj: taskList) {
                taskObj.setStatus(Status.PLANNED);
                taskObj.setUserEntity(task.getUserEntity());
                taskObj.setParent(task);
            }
            return taskRepository.saveAll(taskList);
        } catch (JsonProcessingException e) {
            throw new ErrorResponseException(ErrorStatus.TASK_SUBTASK_GENERATION_ERROR);
        }
    }

    public List<Task> getParentTasksByUserId(long userId, Pageable paging) {
        return taskRepository.findAllByUserEntityIdAndParentIsNull(userId, paging);
    }

    public List<Task> getChildTasksByTaskId(long taskId, Pageable paging) {
        return taskRepository.findAllByParentId(taskId, paging);
    }

    public List<Task> getTasksByTimerId(long timerId) {
        return taskRepository.findAllByTimerId(timerId);
    }

}
