package com.education.gptask.entities.task;

import com.education.gptask.entities.User;
import com.education.gptask.entities.timer.Timer;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany(mappedBy = "tasks",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Timer> timers;

    @ManyToOne
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "parent_id")
    private Task parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Task> childTasks;

    @Column(name = "name")
    private String name;

    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "comment")
    private String comment;

    @Column(name = "updated")
    @UpdateTimestamp
    private LocalDateTime updated;

    @Column(name = "created")
    @CreationTimestamp
    private LocalDateTime created;
}
