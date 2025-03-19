package org.example.todoapi.config;

import org.example.todoapi.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling // Kích hoạt scheduling trong Spring
public class TaskNotificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TaskNotificationScheduler.class);

    @Autowired
    private NotificationService notificationService;

    @Scheduled(fixedRate = 60000) // Chạy mỗi 60 giây (có thể điều chỉnh)
    public void notifyTasks() {
        logger.info("Running scheduled task to check for upcoming/overdue tasks...");
        notificationService.checkAndNotifyTasks();
    }
}
