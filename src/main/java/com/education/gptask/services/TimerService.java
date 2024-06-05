package com.education.gptask.services;

import com.education.gptask.dtos.TimerDto;
import com.education.gptask.dtos.mappers.TimerMapper;
import com.education.gptask.entities.UserEntity;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimerService {
    private final TaskService taskService;
    private final UserService userService;
    private final TimerRepository timerRepository;
    private final TimerMapper timerMapper;

    public List<TimerDto> getTimersDtoByUserId(Long userId) {
        return timerRepository.findTimersByUserEntityId(userId)
                .map(timerMapper::entityListToDtoList)
                .orElseThrow(() -> new ErrorResponseException(ErrorStatus.TIMER_ERROR));
    }
    public List<Timer> getTimersByUserId(Long userId, int messageId) {
        Optional<List<Timer>> timers = timerRepository.findTimersByUserEntityId(userId);
        if (timers.isPresent() && !timers.get().isEmpty()) {
            for (Timer timer: timers.get()) {
                timer.setTelegramMessageId(messageId);
            }

            return timerRepository.saveAll(timers.get());
        } else {
            UserEntity user = userService.getUserByEntityId(userId);
            List<Timer> timerList = new ArrayList<>();
            timerList.add(new Timer().setUserEntity(user).setStatus(TimerStatus.PENDING));
            return timerRepository.saveAll(timerList);
        }
    }

    public TimerDto getTimerDtoById(Long timerId) {
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

    public TimerDto updateTimerDtoStatus(Long timerId, String status) {
        return timerMapper.entityToDto(
                updateTimerStatus(timerId, status));
    }

    @Transactional
    public Timer updateTimerStatus(Long timerId, String status) {
        Timer timer = getTimerById(timerId);
        timer.setStatus(TimerStatus.valueOf(status));
        LocalDateTime stopTime = LocalDateTime.now();
        switch (TimerStatus.valueOf(status)) {
            case PENDING:
                timer.setMinuteToStop(timer.getWorkDuration());
                break;
            case RUNNING:
                //Время остановки
                stopTime = stopTime.plusMinutes(timer.getMinuteToStop());
                timer.setStopTime(stopTime);
                break;
            case PAUSED:
                //Время до остановки
                int timeToStop = (int) Duration.between(LocalDateTime.now(), timer.getStopTime()).getSeconds() / 60;
                timer.setMinuteToStop(timeToStop);
                break;
            case LONG_BREAK:
                break;
            case SHORT_BREAK:
                break;
        }
        return timerRepository.save(timer);
    }

    //Return: null - if no timer has expired
    public List<Timer> getExpiredTimersAndUpdate() {
        Optional<List<Timer>> expiredTimers = timerRepository.findAllExpiredAndNotPending(LocalDateTime.now());
        if (!expiredTimers.isPresent()) return null;

        List<Long> ids = expiredTimers.get().stream().map(Timer::getId).collect(Collectors.toList());
        if (!ids.isEmpty()) {
            timerRepository.updateStatusToPending(ids);
        }
        return expiredTimers.get();
    }

    public void deleteTimerById(Long timerId) {
        timerRepository.deleteById(timerId);
    }

    @Transactional
    public TimerDto bindTaskToTimer(Long timerId, Long taskId) {
        Timer timer = getTimerById(timerId);
        Task task = taskService.getTaskById(taskId);
        if (!timer.getUserEntity().getId()
                .equals(task.getUserEntity().getId())) {
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
