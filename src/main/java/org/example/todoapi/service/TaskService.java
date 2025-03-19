package org.example.todoapi.service;

import jakarta.transaction.Transactional;
import org.example.todoapi.entity.Task;
import org.example.todoapi.entity.TaskDependency;
import org.example.todoapi.exception.TaskException;
import org.example.todoapi.repository.TaskDependencyRepository;
import org.example.todoapi.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskDependencyRepository dependencyRepository;

    @Autowired
    private CacheManager cacheManager; // Inject CacheManager

    @CacheEvict(value = {"tasks", "dependencies"}, allEntries = true)
    public Task createTask(Task task) {
        logger.info("Creating task and clearing cache: {}", task.getTitle());
        return taskRepository.save(task);
    }

    @Cacheable(value = "tasks", key = "#page + '-' + #size + '-' + #status.orElse('ALL')")
    public Page<Task> getTasks(int page, int size, Optional<Task.Status> status) {
        logger.info("Fetching tasks from database: page={}, size={}, status={}", page, size, status);
        PageRequest pageRequest = PageRequest.of(page, size);
        return taskRepository.findTasksByStatusOptional(status.orElse(null), pageRequest);
    }

    public Task updateTask(Long id, Task taskDetails) {
        logger.info("Updating task and clearing cache: id={}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskException("Task not found", HttpStatus.NOT_FOUND.value()));
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setDueDate(taskDetails.getDueDate());
        task.setPriority(taskDetails.getPriority());
        task.setStatus(taskDetails.getStatus());

        // Xóa cache thủ công
        Objects.requireNonNull(cacheManager.getCache("tasks")).clear(); // Xóa toàn bộ tasks
        Objects.requireNonNull(cacheManager.getCache("dependencies")).evict(id); // Xóa dependency của task cụ thể

        return taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        logger.info("Deleting task and clearing cache: id={}", id);
        if (!taskRepository.existsById(id)) {
            throw new TaskException("Task not found", HttpStatus.NOT_FOUND.value());
        }
        dependencyRepository.deleteByTaskIdOrDependsOnId(id);

        // Xóa cache thủ công
        Objects.requireNonNull(cacheManager.getCache("tasks")).clear(); // Xóa toàn bộ tasks
        Objects.requireNonNull(cacheManager.getCache("dependencies")).evict(id); // Xóa dependency của task cụ thể

        taskRepository.deleteById(id);
    }

    @Transactional
    @CacheEvict(value = "dependencies", allEntries = true)
    public TaskDependency addDependency(Long taskId, Long dependsOnId) {
        logger.info("Adding dependency and clearing cache: taskId={}, dependsOnId={}", taskId, dependsOnId);
        if (!taskRepository.existsById(taskId)) {
            throw new TaskException("Task not found", HttpStatus.NOT_FOUND.value());
        }
        if (!taskRepository.existsById(dependsOnId)) {
            throw new TaskException("Dependency task not found", HttpStatus.NOT_FOUND.value());
        }

        if (hasCircularDependency(taskId, dependsOnId)) {
            throw new TaskException("Circular dependency detected", HttpStatus.BAD_REQUEST.value());
        }

        if (dependencyRepository.existsByTaskIdAndDependsOnId(taskId, dependsOnId)) {
            throw new TaskException("Dependency already exists", HttpStatus.CONFLICT.value());
        }

        TaskDependency dependency = new TaskDependency();
        dependency.setTaskId(taskId);
        dependency.setDependsOnId(dependsOnId);
        return dependencyRepository.save(dependency);
    }

    @Cacheable(value = "dependencies", key = "#taskId")
    public Set<Long> getAllDependencies(Long taskId) {
        logger.info("Fetching dependencies from database: taskId={}", taskId);
        if (!taskRepository.existsById(taskId)) {
            throw new TaskException("Task not found", HttpStatus.NOT_FOUND.value());
        }
        Set<Long> dependencies = new HashSet<>();
        getDependenciesRecursive(taskId, dependencies);
        return dependencies;
    }

    private void getDependenciesRecursive(Long taskId, Set<Long> dependencies) {
        List<TaskDependency> directDeps = dependencyRepository.findByTaskId(taskId);
        for (TaskDependency dep : directDeps) {
            Long depId = dep.getDependsOnId();
            if (!dependencies.contains(depId)) {
                dependencies.add(depId);
                getDependenciesRecursive(depId, dependencies);
            }
        }
    }

    private boolean hasCircularDependency(Long taskId, Long dependsOnId) {
        Set<Long> visited = new HashSet<>();
        return checkCircularRecursive(taskId, dependsOnId, visited);
    }

    private boolean checkCircularRecursive(Long startId, Long currentId, Set<Long> visited) {
        if (startId.equals(currentId)) return true;

        visited.add(currentId);
        List<TaskDependency> dependencies = dependencyRepository.findByTaskId(currentId);

        for (TaskDependency dep : dependencies) {
            Long nextId = dep.getDependsOnId();
            if (!visited.contains(nextId) && checkCircularRecursive(startId, nextId, visited)) {
                return true;
            }
        }
        return false;
    }
}