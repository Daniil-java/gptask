package com.education.gptask.dtos.mappers;

import com.education.gptask.dtos.TimerDto;
import com.education.gptask.entities.timer.Timer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TimerMapper {
    TimerDto entityToDto(Timer timer);

    Timer dtoToEntity(TimerDto timerDto);
}
