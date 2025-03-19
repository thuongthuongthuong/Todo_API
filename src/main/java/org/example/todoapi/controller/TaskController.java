package org.example.todoapi.controller;

import org.example.todoapi.entity.Task;
import org.example.todoapi.entity.TaskDependency;
import org.example.todoapi.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        return ResponseEntity.ok(taskService.createTask(task));
    }

    @GetMapping
    public ResponseEntity<Page<Task>> getTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Task.Status status) {
        return ResponseEntity.ok(taskService.getTasks(page, size, Optional.ofNullable(status)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        return ResponseEntity.ok(taskService.updateTask(id, task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/dependencies")
    public ResponseEntity<TaskDependency> addDependency(
            @RequestParam Long taskId,
            @RequestParam Long dependsOnId) {
        return ResponseEntity.ok(taskService.addDependency(taskId, dependsOnId));
    }

    @GetMapping("/{id}/dependencies")
    public ResponseEntity<Set<Long>> getDependencies(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getAllDependencies(id));
    }
}
