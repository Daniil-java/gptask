package com.education.gptask.dtos;

import com.education.gptask.entities.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@Accessors(chain = true)
public class UserDto {
    @NotNull(message = "User id can't be empty!")
    private Long id;
    private String password;
    private Set<Role> roles;
    private Long telegramId;
    private Long chatId;
    private String username;
}
