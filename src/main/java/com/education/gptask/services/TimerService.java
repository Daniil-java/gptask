package com.education.gptask.services;

import com.education.gptask.dtos.TimerDto;
import com.education.gptask.dtos.mappers.TimerMapper;
import com.education.gptask.entities.error.ErrorResponseException;
import com.education.gptask.entities.error.ErrorStatus;
import com.education.gptask.entities.timer.Timer;
import com.education.gptask.repositories.TimerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimerService {
    private final TimerRepository timerRepository;
    private final TimerMapper timerMapper;

    public TimerDto getTimerByUserId(Long userId) {
        return timerMapper.entityToDto(
                timerRepository.findTimerByUserId(userId)
                        .orElseThrow(() -> new ErrorResponseException(ErrorStatus.TIMER_ERROR))
        );
    }

    public TimerDto getTimerById(Long timerId) {
        return timerMapper.entityToDto(
                timerRepository.getReferenceById(timerId)
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

    public TimerDto updateTimerStatus(Long timerId, TimerDto timerDto) {
        return timerMapper.entityToDto(
                timerRepository.save(new Timer().setId(timerId).setStatus(timerDto.getStatus()))
        );
    }

    public void deleteTimerById(Long timerId) {
        timerRepository.deleteById(timerId);
    }
}
