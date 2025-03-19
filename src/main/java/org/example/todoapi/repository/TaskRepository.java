package org.example.todoapi.repository;

import org.example.todoapi.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByStatus(Task.Status status, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE (:status IS NULL OR t.status = :status)")
    Page<Task> findTasksByStatusOptional(Task.Status status, Pageable pageable);

    List<Task> findByStatusNot(Task.Status status); // Lấy task chưa hoàn thành
}
