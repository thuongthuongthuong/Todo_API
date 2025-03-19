package org.example.todoapi.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class TaskDependency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "depends_on_id", nullable = false)
    private Long dependsOnId;
}