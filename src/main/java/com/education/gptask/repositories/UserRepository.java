package com.education.gptask.repositories;

import com.education.gptask.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findUserByChatId(Long chatId);
    Optional<UserEntity> findUserEntityByTelegramId(Long telegramId);
}
