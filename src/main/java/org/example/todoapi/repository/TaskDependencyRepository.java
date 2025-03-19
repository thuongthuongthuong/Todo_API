package org.example.todoapi.repository;

import org.example.todoapi.entity.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {
    // Tìm tất cả dependency của một task
    List<TaskDependency> findByTaskId(Long taskId);

    // Tìm tất cả các task phụ thuộc vào một task cụ thể
    List<TaskDependency> findByDependsOnId(Long dependsOnId);

    // Xóa tất cả dependency liên quan đến một task
    @Modifying
    @Query("DELETE FROM TaskDependency td WHERE td.taskId = :taskId OR td.dependsOnId = :taskId")
    void deleteByTaskIdOrDependsOnId(Long taskId);

    // Kiểm tra xem một dependency cụ thể đã tồn tại chưa
    boolean existsByTaskIdAndDependsOnId(Long taskId, Long dependsOnId);
}