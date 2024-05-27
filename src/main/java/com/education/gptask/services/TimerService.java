package com.education.gptask.services;

import com.education.gptask.dtos.TimerDto;
import com.education.gptask.dtos.mappers.TimerMapper;
import com.education.gptask.entities.error.ErrorResponseException;
import com.education.gptask.entities.error.ErrorStatus;
import com.education.gptask.entities.timer.Timer;
import com.education.gptask.entities.timer.TimerStatus;
import com.education.gptask.repositories.TimerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimerService {
    private final TimerRepository timerRepository;
    private final TimerMapper timerMapper;

    public List<TimerDto> getTimersByUserId(Long userId) {
        return timerMapper.entityListToDtoList(
                timerRepository.findTimersByUser_Id(userId)
                .orElseThrow(() -> new ErrorResponseException(ErrorStatus.TIMER_ERROR))
        );
    }

    public TimerDto getTimerById(Long timerId) {
        return timerMapper.entityToDto(
                timerRepository.findById(timerId)
                        .orElseThrow(() -> new ErrorResponseException(ErrorStatus.TIMER_ERROR))
        );
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
}
