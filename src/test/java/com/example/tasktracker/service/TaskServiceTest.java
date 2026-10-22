package com.example.tasktracker.service;

import com.example.tasktracker.exception.TaskNotFoundException;
import com.example.tasktracker.model.Task;
import com.example.tasktracker.repository.TaskRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskServiceTest {

    @Test
    void missingTaskThrowsTaskNotFoundException() {
        TaskService service = new TaskService(new TaskRepository());

        assertThrows(TaskNotFoundException.class, () -> service.findById(999L));
    }

    @Test
    void setCompletionPersistsTheRequestedStatus() {
        TaskService service = new TaskService(new TaskRepository());
        Task task = service.save(new Task(null, "Finish lab", "Test completion", false));

        service.setCompletion(task.getId(), true);
        assertTrue(service.findById(task.getId()).isCompleted());

        service.setCompletion(task.getId(), false);
        assertFalse(service.findById(task.getId()).isCompleted());
    }

    @Test
    void deleteRemovesOnlyTheSelectedTask() {
        TaskService service = new TaskService(new TaskRepository());
        Task taskToDelete = service.save(new Task(null, "Delete me", "Delete test", false));
        Task taskToKeep = service.save(new Task(null, "Keep me", "Keep test", false));

        service.delete(taskToDelete.getId());

        assertThrows(TaskNotFoundException.class, () -> service.findById(taskToDelete.getId()));
        assertTrue(service.findById(taskToKeep.getId()).getTitle().equals("Keep me"));
    }

    @Test
    void deletingMissingTaskThrowsTaskNotFoundException() {
        TaskService service = new TaskService(new TaskRepository());

        assertThrows(TaskNotFoundException.class, () -> service.delete(999L));
    }
}
