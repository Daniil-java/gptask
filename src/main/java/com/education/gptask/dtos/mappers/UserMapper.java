package com.education.gptask.dtos.mappers;

import com.education.gptask.dtos.UserDto;
import com.education.gptask.entities.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto entityToDto(UserEntity userEntity);
    UserEntity dtoToEntity(UserDto userDto);
}
