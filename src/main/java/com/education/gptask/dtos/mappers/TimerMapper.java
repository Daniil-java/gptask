package com.education.gptask.dtos.mappers;

import com.education.gptask.dtos.TaskDto;
import com.education.gptask.dtos.TimerDto;
import com.education.gptask.dtos.UserDto;
import com.education.gptask.entities.UserEntity;
import com.education.gptask.entities.task.Task;
import com.education.gptask.entities.timer.Timer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class, TaskMapper.class})
public interface TimerMapper {
    @Mapping(source = "userEntity", target = "user")
    @Mapping(source = "tasks", target = "tasks")
    TimerDto entityToDto(Timer timer);

    @Mapping(source = "user", target = "userEntity")
    @Mapping(source = "tasks", target = "tasks")
    Timer dtoToEntity(TimerDto timerDto);

    List<TimerDto> entityListToDtoList(List<Timer> timerList);
    List<Timer> dtoListToEntityList(List<TimerDto> timerDtoList);

    default UserDto mapUser(UserEntity user, UserMapper userMapper) {
        if (user == null) {
            return null;
        }
        return userMapper.entityToDto(user);
    }

    default UserEntity mapUserDto(UserDto userDto, UserMapper userMapper) {
        if (userDto == null) {
            return null;
        }
        return userMapper.dtoToEntity(userDto);
    }

    default List<TaskDto> mapTasks(List<Task> taskList, TaskMapper taskMapper) {
        if (taskList == null) {
            return null;
        }
        return taskMapper.entityListToDtoList(taskList);
    }

    default List<Task> mapTasksDto(List<TaskDto> taskDtoList, TaskMapper taskMapper) {
        if (taskDtoList == null) {
            return null;
        }
        return taskMapper.dtoListToEntityList(taskDtoList);
    }
}
