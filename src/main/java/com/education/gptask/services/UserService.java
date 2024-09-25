package com.education.gptask.services;

import com.education.gptask.dtos.UserDto;
import com.education.gptask.dtos.mappers.UserMapper;
import com.education.gptask.entities.Role;
import com.education.gptask.entities.UserEntity;
import com.education.gptask.entities.error.ErrorResponseException;
import com.education.gptask.entities.error.ErrorStatus;
import com.education.gptask.repositories.RoleRepository;
import com.education.gptask.repositories.UserRepository;
import com.education.gptask.telegram.entities.BotState;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public boolean registerUser(UserDto userDto) {
        return registerUser(userMapper.dtoToEntity(userDto));
    }
    public boolean registerUser(UserEntity userEntity) {
        Optional<UserEntity> userOpt = userRepository.findByUsername(userEntity.getUsername());

        if (userOpt.isPresent()) {
            return false;
        }

        userRepository.save(new UserEntity()
                .setRoles(Collections.singleton(new Role().setId(1L).setStatus("ROLE_USER")))
                .setPassword(bCryptPasswordEncoder.encode(userEntity.getPassword()))
                .setUsername(userEntity.getUsername())
        );
        return true;
    }

    public UserDto getUserDtoByUsername(String username) {
        return userMapper.entityToDto(getUserByUsername(username));
    }

    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ErrorResponseException(ErrorStatus.USER_NOT_FOUND_ERROR));
    }
    public UserEntity updateUserEntity(UserEntity userEntity) {
        return userRepository.save(userEntity);
    }
    @Transactional
    public UserEntity getOrCreateUser(User userInfo, BotState botState) {
        Optional<UserEntity> userEntity = userRepository.findUserEntityByTelegramId(userInfo.getId());
        if (userEntity.isPresent()) {
            if (botState != null) {
                userEntity.get().setBotState(botState);
            }
            return userRepository.save(userEntity.get());
        } else {
            return userRepository.save(new UserEntity()
                    .setTelegramId(userInfo.getId())
                    .setChatId(userInfo.getId())
                    .setUsername(userInfo.getUserName())
                    .setFirstname(userInfo.getFirstName())
                    .setLastname(userInfo.getLastName())
                    .setLanguageCode(userInfo.getLanguageCode())
                    .setBotState(botState)
            );
        }
    }

    public UserEntity createUser(UserEntity userEntity) {
        return userRepository.save(userEntity);
    }

    public UserEntity getUserByTelegramId(Long telegramId) {
        return userRepository.findUserEntityByTelegramId(telegramId)
                .orElseThrow(() -> new ErrorResponseException(ErrorStatus.USER_NOT_FOUND_ERROR));
    }

    public UserEntity getUserByTelegramChatId(Long telegramChatId) {
        return userRepository.findUserByChatId(telegramChatId)
                .orElseThrow(() -> new ErrorResponseException(ErrorStatus.USER_NOT_FOUND_ERROR));
    }

    public UserEntity getUserByEntityId(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ErrorResponseException(ErrorStatus.USER_NOT_FOUND_ERROR));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ErrorResponseException(ErrorStatus.USER_NOT_FOUND_ERROR));
    }
}
