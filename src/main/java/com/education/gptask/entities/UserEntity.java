package com.education.gptask.entities;

import com.education.gptask.telegram.enteties.BotState;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long telegramId;
    private Long chatId;
    @Enumerated(EnumType.STRING)
    private BotState botState;
    private String username;
    private String firstname;
    private String lastname;
    private String languageCode;
    @CreationTimestamp
    private LocalDateTime created;
}
