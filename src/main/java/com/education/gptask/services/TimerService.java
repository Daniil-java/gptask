package com.education.gptask.services;

import com.education.gptask.dtos.TimerDto;
import com.education.gptask.dtos.mappers.TimerMapper;
import com.education.gptask.entities.error.ErrorResponseException;
import com.education.gptask.entities.error.ErrorStatus;
import com.education.gptask.entities.task.Task;
import com.education.gptask.entities.timer.Timer;
import com.education.gptask.entities.timer.TimerStatus;
import com.education.gptask.repositories.TimerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimerService {
    private final TaskService taskService;
    private final TimerRepository timerRepository;
    private final TimerMapper timerMapper;

    public List<TimerDto> getTimersByUserId(Long userId) {
        return timerMapper.entityListToDtoList(
                timerRepository.findTimersByUser_Id(userId)
                .orElseThrow(() -> new ErrorResponseException(ErrorStatus.TIMER_ERROR))
        );
    }

    public TimerDto getTimerByIdResponse(Long timerId) {
        return timerMapper.entityToDto(getTimerById(timerId));
    }

    private Timer getTimerById(Long timerId) {
        return timerRepository.findById(timerId)
                .orElseThrow(() -> new ErrorResponseException(ErrorStatus.TIMER_ERROR));
    }

    public TimerDto createTimer(TimerDto timerDto) {
        return timerMapper.entityToDto(
                timerRepository.save(timerMapper.dtoToEntity(timerDto))
        );
    }

    public TimerDto updateTimer(TimerDto timerDto) {
        if (timerDto.getId() == null) {
            throw new ErrorResponseException(ErrorStatus.TIMER_UPDATE_ERROR);
        }
        return timerMapper.entityToDto(
                timerRepository.save(timerMapper.dtoToEntity(timerDto))
        );
    }

    public TimerDto updateTimerStatus(Long timerId, String status) {
        return timerMapper.entityToDto(
                timerRepository.save(new Timer()
                        .setId(timerId)
                        .setStatus(TimerStatus.valueOf(status.toUpperCase())))
        );
    }

    public void deleteTimerById(Long timerId) {
        timerRepository.deleteById(timerId);
    }

    @Transactional
    public TimerDto bindTaskToTimer(Long timerId, Long taskId) {
        Timer timer = getTimerById(timerId);
        Task task = taskService.getTaskById(taskId);
        if (!timer.getUser().getId()
                .equals(task.getUser().getId())) {
            throw new ErrorResponseException(ErrorStatus.TIMER_TASK_ERROR);
        }
        timer.getTasks().add(task);
        return timerMapper.entityToDto(
                timerRepository.save(timer)
        );
    }

    public TimerDto unbindTaskFromTimer(Long timerId, Long taskId) {
        Timer timer = getTimerById(timerId);
        Set<Task> taskList = timer.getTasks();

        Optional<Task> taskOptional = taskList.stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst();

        if (taskOptional.isPresent()) {
            taskList.remove(taskOptional.get());
        } else {
            throw new ErrorResponseException(ErrorStatus.TIMER_TASK_ERROR);
        }
        return timerMapper.entityToDto(timerRepository.save(timer));
    }

    public TimerDto unbindAllTasksFromTimer(Long timerId) {
        Timer timer = getTimerById(timerId);
        timer.getTasks().clear();
        return timerMapper.entityToDto(timerRepository.save(timer));
    }
}
