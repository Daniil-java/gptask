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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final String botToken = System.getenv("TGBOT_TOKEN");

    public boolean registerUser(UserDto userDto) {
        return registerUser(userMapper.dtoToEntity(userDto));
    }
    public boolean registerUser(UserEntity userEntity) {
        Optional<UserEntity> userOpt = userRepository.findByUsername(userEntity.getUsername());

        if (userOpt.isPresent()) {
            return false;
        }

        userRepository.save(new UserEntity()
                .setRoles(Collections.singleton(new Role().setStatus("ROLE_USER")))
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

    public UserDto getOrCreateTelegramUser(String telegramId, String username) {
        long tgId = Long.parseLong(telegramId);
        Optional<UserEntity> userEntity =
                userRepository.findUserEntityByTelegramId(tgId);
        UserEntity user;
        if (!userEntity.isPresent()) {
            user = userRepository.save(new UserEntity()
                    .setTelegramId(tgId)
                    .setRoles(Collections.singleton(new Role().setId(1L).setStatus("ROLE_USER")))
                    .setUsername(username)
            );
        } else {
            user = userEntity.get();
        }
        return userMapper.entityToDto(user);
    }

    public void handleTelegramAuth(Map<String, String> queryParams) {
        // Проверьте данные, пришедшие от Telegram (подпись и другие параметры)
        if (isValidTelegramAuth(queryParams)) {
            String telegramId = queryParams.get("id");
            String username = queryParams.get("username");

            // Получаем или создаем пользователя
            UserDto userDto = getOrCreateTelegramUser(telegramId, username);

            // Создаем объект UserDetails
            UserDetails userDetails = new UserEntity().setUsername(username);

            // Создаем аутентификационный объект
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            // Сохраняем объект аутентификации в SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    private boolean isValidTelegramAuth(Map<String, String> queryParams) {
        // Получите подпись, присланную Telegram
        String hash = queryParams.get("hash");

        // Уберите поле 'hash', так как его не надо учитывать при проверке подписи
        Map<String, String> authData = new TreeMap<>(queryParams);
        authData.remove("hash");

        // Подготовьте строку для проверки
        StringBuilder dataCheckString = new StringBuilder();
        for (Map.Entry<String, String> entry : authData.entrySet()) {
            dataCheckString.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }

        // Удаляем последний перенос строки
        dataCheckString.deleteCharAt(dataCheckString.length() - 1);

        try {
            // Генерация секретного ключа для подписи
            byte[] secretKey = MessageDigest.getInstance("SHA-256").digest(botToken.getBytes(StandardCharsets.UTF_8));

            // Создайте HMAC_SHA256 хеш
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA256");
            mac.init(secretKeySpec);

            // Примените хеширование
            byte[] computedHash = mac.doFinal(dataCheckString.toString().getBytes(StandardCharsets.UTF_8));

            // Сравните хеши (телеграм присылает hash в hex-формате)
            String computedHashHex = bytesToHex(computedHash);
            return computedHashHex.equals(hash);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Вспомогательный метод для конвертации байтов в hex-строку
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
