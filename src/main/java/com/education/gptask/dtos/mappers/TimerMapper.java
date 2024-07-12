package com.education.gptask.dtos.mappers;

import com.education.gptask.dtos.TimerDto;
import com.education.gptask.entities.timer.Timer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TimerMapper {
    @Mapping(ignore = true, target = "tasks")
    TimerDto entityToDto(Timer timer);

    Timer dtoToEntity(TimerDto timerDto);
    @Mapping(ignore = true, target = "tasks")
    List<TimerDto> entityListToDtoList(List<Timer> timerList);
    List<Timer> dtoListToEntityList(List<TimerDto> timerDtoList);
}
