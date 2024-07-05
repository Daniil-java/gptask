package com.education.gptask.repositories;

import com.education.gptask.entities.task.Task;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<List<Task>> findTasksByUserEntityIdAndParentIsNull(Long id);

    @Query("SELECT t FROM Task t WHERE t.id > :startId AND t.parent IS NULL AND t.userEntity.id = :userId ORDER BY t.id ASC")
    List<Task> findTasksAfterId(@Param("startId") Long startId, @Param("userId") Long userId, Pageable pageable);

    default Optional<List<Task>> findTasksAfterIdLimited(Long userId, Long startId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Task> tasks = findTasksAfterId(startId, userId, pageable);
        return Optional.ofNullable(tasks);
    }

    @Query("SELECT t FROM Task t WHERE t.id < :endId AND t.parent IS NULL AND t.userEntity.id = :userId ORDER BY t.id DESC")
    List<Task> findTasksBeforeId(@Param("endId") Long endId, @Param("userId") Long userId, Pageable pageable);

    default Optional<List<Task>> findTasksBeforeIdLimited(Long userId, Long endId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Task> tasks = findTasksBeforeId(endId, userId, pageable);
        Collections.reverse(tasks);
        return Optional.ofNullable(tasks);
    }
}
