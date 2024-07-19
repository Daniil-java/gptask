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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.education.gptask.entities.timer.TimerStatus.PAUSED;
import static com.education.gptask.entities.timer.TimerStatus.PENDING;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimerService {
    private final TaskService taskService;
    private final UserService userService;
    private final TimerRepository timerRepository;
    private final TimerMapper timerMapper;

    public List<TimerDto> getTimersDtoByUserId(Long userId) {
        return timerMapper.entityListToDtoList(getTimersByUserId(userId));
    }

    public List<Timer> getTimersByUserId(Long userId) {
        return timerRepository.findTimersByUserEntityId(userId)
                .orElseThrow(() -> new ErrorResponseException(ErrorStatus.TIMER_ERROR));
    }
    public List<Timer> getOrCreateTimerByUserId(long userId) {
        return getTimersByUserId(userId, 0);
    }
    public List<Timer> getTimersByUserId(Long userId, int messageId) {
        Optional<List<Timer>> timers = timerRepository.findTimersByUserEntityId(userId);
        if (timers.isPresent() && !timers.get().isEmpty()) {
            if (messageId != 0) timers.get().get(0).setTelegramMessageId(messageId);
            return timerRepository.saveAll(timers.get());
        } else {
            UserEntity user = userService.getUserByEntityId(userId);
            List<Timer> timerList = timers.get();
            timerList.add(new Timer().setUserEntity(user).setStatus(TimerStatus.PENDING));
            return timerRepository.saveAll(timerList);
        }
    }

    public void resetIntervalById(Long timerId) {
        timerRepository.resetIntervalById(timerId);
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

    public TimerDto updateTimerDto(TimerDto timerDto) {
        return timerMapper.entityToDto(updateTimer(timerMapper.dtoToEntity(timerDto)));
    }

    public Timer updateTimer(Timer timer) {
        if (timer.getId() == null) {
            throw new ErrorResponseException(ErrorStatus.TIMER_UPDATE_ERROR);
        }
        return timerRepository.save(timer);
    }

    public TimerDto updateTimerDtoStatus(Long timerId, String status) {
        return timerMapper.entityToDto(
                updateTimerStatus(timerId, status));
    }

    /**
     * Находит таймеры с истекшим временем
     * В зависимости от настроек, имеет логику
     * автоматического изменения статуса таймера
     * @return null - if no timer has expired
     */
    @Transactional
    public List<Timer> getExpiredTimersAndUpdate() {
        Optional<List<Timer>> expiredTimers = timerRepository.findAllExpired(LocalDateTime.now());
        if (!expiredTimers.isPresent()) return null;

        List<Timer> timers = expiredTimers.get();
        for (Timer timer: timers) {
            timer.setInterval(timer.getInterval() + 1);
            timer.setMinuteToStop(0);
            updateTimer(timer);
            //Автоматический запуск таймера, если установлены необходимые настройки
            if ((timer.isAutostartBreak() && timer.getInterval() % 2 == 0) ||
                    (timer.isAutostartWork() && timer.getInterval() % 2 != 0)) {
                updateTimerStatus(timer.getId(), TimerStatus.RUNNING.name());
            } else {
                updateTimerStatus(timer.getId(), PAUSED.name());
            }
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

    /**
     * Обновление статуса таймера.
     * PENDING:
     *      Приводит таймер к нерабочему состоянию.
     *      Обнуляет время остановки. При запуске,
     *      в зависимости от интервала, время до
     *      остановки будет вычислено исходя из
     *      времени интервала
     * RUNNING:
     *      Если время ДО остановки не обнуленно,
     *      вычисляет точное время остановки, исходя
     *      из этого значения.
     *      Если время до остановки не имеет значения,
     *      вычисляет время остановки исходя из текущего
     *      интервала.
     *      Чётный интервал - время работы
     *      Нечётный интервал - пауза
     * PAUSED:
     *      Вычисляет оставшееся время до остановки и
     *      сохраняет.
     * @param timerId
     * @param status
     * @return
     */
    @Transactional
    public Timer updateTimerStatus(Long timerId, String status) {
        Timer timer = getTimerById(timerId);
        timer.setStatus(TimerStatus.valueOf(status));
        LocalDateTime stopTime = LocalDateTime.now();
        switch (TimerStatus.valueOf(status)) {
            case PENDING:
                timer.setMinuteToStop(timer.getWorkDuration());
                timer.setStopTime(null);
                break;
            case RUNNING:
                if (timer.getMinuteToStop() != 0) {
                    stopTime = stopTime.plusMinutes(timer.getMinuteToStop());
                } else if (timer.getLongBreakInterval() != 0 && timer.getInterval() % 2 != 0
                        && timer.getInterval() / 2 % timer.getLongBreakInterval() != 0) {
                    stopTime = stopTime.plusMinutes(timer.getLongBreakDuration());
                } else if (timer.getLongBreakInterval() != 0 && timer.getInterval() % 2 != 0 ) {
                    stopTime = stopTime.plusMinutes(timer.getShortBreakDuration());
                } else {
                    stopTime = stopTime.plusMinutes(timer.getWorkDuration());
                }
                timer.setStopTime(stopTime);
                break;
            case PAUSED:
                if (timer.getStopTime() == null) {
                    return updateTimerStatus(timerId, String.valueOf(PENDING));
                }
                int timeToStop = (int) Duration.between(LocalDateTime.now(), timer.getStopTime()).getSeconds() / 60;
                timer.setMinuteToStop(timeToStop);
                timer.setStopTime(null);
                break;
        }
        return timerRepository.save(timer);
    }
}
