package com.education.gptask.entities.timer;

import com.education.gptask.entities.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "timers")
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Timer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private TimerStatus status;

    @Column(name = "workDuration")
    private int workDuration;

    @Column(name = "shortBreakDuration")
    private int shortBreakDuration;

    @Column(name = "longBreakDuration")
    private int longBreakDuration;

    @Column(name = "longBreakInterval")
    private int longBreakInterval;

    @Column(name = "isAutostartWork")
    private boolean isAutostartWork;

    @Column(name = "isAutostartBreak")
    private boolean isAutostartBreak;

    @Column(name = "updated")
    @UpdateTimestamp
    private LocalDateTime updated;

    @Column(name = "created")
    @CreationTimestamp
    private LocalDateTime created;


}
