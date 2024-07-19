package com.education.gptask.dtos.mappers;

import com.education.gptask.dtos.TimerDto;
import com.education.gptask.entities.timer.Timer;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TimerMapper {
    TimerDto entityToDto(Timer timer);
    Timer dtoToEntity(TimerDto timerDto);
    List<TimerDto> entityListToDtoList(List<Timer> timerList);
    List<Timer> dtoListToEntityList(List<TimerDto> timerDtoList);
}
