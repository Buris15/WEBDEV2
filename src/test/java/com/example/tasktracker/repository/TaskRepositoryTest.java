package com.example.tasktracker.repository;

import com.example.tasktracker.model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TaskRepositoryTest {

    @Test
    void saveAssignsAnIncrementingId() {
        TaskRepository repository = new TaskRepository();
        Task task = repository.save(new Task(null, "New task", "Test description", false));

        assertNotNull(task.getId());
        assertEquals(1L, task.getId());
    }
}
