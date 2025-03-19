package org.example.todoapi.service;

import org.example.todoapi.entity.Task;
import org.example.todoapi.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private TaskRepository taskRepository;

    public void checkAndNotifyTasks() {
        // Lấy tất cả task chưa hoàn thành
        List<Task> pendingTasks = taskRepository.findByStatusNot(Task.Status.COMPLETED);

        for (Task task : pendingTasks) {
            if (task.isOverdue()) {
                logger.warn("Task overdue: ID={}, Title={}, Due Date={}",
                        task.getId(), task.getTitle(), task.getDueDate());
            } else if (task.isUpcoming(24)) { // Thông báo trước 24 giờ
                logger.info("Task upcoming: ID={}, Title={}, Due Date={}",
                        task.getId(), task.getTitle(), task.getDueDate());
            }
        }
    }
}