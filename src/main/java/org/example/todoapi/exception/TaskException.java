package org.example.todoapi.exception;

public class TaskException extends RuntimeException {
    private final int status;

    public TaskException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
