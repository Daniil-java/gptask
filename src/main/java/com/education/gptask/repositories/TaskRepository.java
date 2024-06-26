package com.education.gptask.repositories;

import com.education.gptask.entities.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<List<Task>> findTasksByUserIdAndParentIsNull(Long id);
}
