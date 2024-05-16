package com.education.gptask.dtos.mappers;

import com.education.gptask.dtos.UserDto;
import com.education.gptask.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto entityToDto(User user);
    User dtoToEntity(UserDto userDto);
}
