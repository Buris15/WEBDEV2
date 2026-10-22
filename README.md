# WEBDEV2 Prelim Lab Exam — Task Tracker

A fresh Spring Boot MVC application built for the Task Tracker specification. It uses Java 17, Maven Wrapper, Spring Web, Thymeleaf, Bean Validation, constructor injection, and an in-memory repository. No database or JPA is used.

## Run on Windows

1. Extract this ZIP.
2. Open the extracted `webdev2-prelim-task-tracker-fixed` folder in VS Code.
3. Open **Terminal → New Terminal**.
4. Run:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

5. Open http://localhost:8080/tasks

The first run needs an internet connection for Maven to download dependencies. Stop the server with `Ctrl+C`.

## Routes to demonstrate

- `/tasks` — list all tasks
- `/tasks/1` — view a task
- `/tasks/new` — show the validated create form
- `POST /tasks` — save a valid task and redirect to `/tasks`
- `/tasks/999` — display the custom not-found page
- `POST /tasks/{id}/completion` — set a task explicitly to completed or in progress
- `POST /tasks/{id}/delete` — delete a task after a confirmation prompt

## Validation checks

- Submit an empty title: required and length errors appear under the title field.
- Enter a one-character title: validation error appears.
- Enter a title between 2 and 100 characters and an optional description up to 500 characters: task is saved and the browser redirects to `/tasks`.
- A description longer than 500 characters is rejected with an inline error.

## Architecture

- `model/Task.java`: encapsulated fields, constructor, getters/setters, validation constraints.
- `repository/TaskRepository.java`: in-memory `List<Task>`, incrementing IDs, `findAll`, `findById`, and `save`.
- `service/TaskService.java`: application logic and constructor-injected repository.
- `controller/TaskController.java`: routes, form binding, validation, redirect, exception handler.
- `exception/TaskNotFoundException.java`: custom missing-task exception.
- Thymeleaf templates: list, detail, validated form, and not-found page.

The repository starts empty so the list page can demonstrate its empty-state message. Create a task to demonstrate the detail page. Data exists only while the application is running and resets when it restarts.


## Task status and deletion

New tasks start as **In progress** (`completed = false`). Use **Mark complete** on the list or detail page to set the status to **Completed**, or **Mark in progress** to set it back to `false`. Each button sends the intended status explicitly rather than toggling the current value.

Use **Delete** on the list or detail page to remove a task. The browser asks for confirmation first. Deleting a task redirects to the task list, and a deleted task can no longer be opened by ID. This is an optional extra feature beyond the exam's required list, detail, create, validation, and not-found flows.
