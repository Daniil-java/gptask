package com.education.gptask.entities;

import com.education.gptask.entities.task.Task;
import com.education.gptask.entities.timer.Timer;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@Table(name = "timer_tasks")
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class TimerTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timer_id")
    private Timer timer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;
}
