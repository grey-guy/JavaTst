package com.developer.test.controller;

import com.developer.test.dto.TasksResponse;
import com.developer.test.model.Task;
import com.developer.test.service.DataStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final DataStore dataStore;

    public TaskController(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @GetMapping
    public ResponseEntity<TasksResponse> getTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userId) {
        List<com.developer.test.model.Task> tasks = dataStore.getTasks(status, userId);
        TasksResponse response = new TasksResponse(tasks, tasks.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task request) {
        try {
            // Validate required fields
            if (isNullOrEmpty(request.getTitle())
                    || isNullOrEmpty(request.getStatus())
                    || request.getUserId() <= 0) {

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .build();
            }

            // Validate status
            if (!isValidStatus(request.getStatus())) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .build();
            }

            // Validate userId exists
            if (!dataStore.userExists(request.getUserId())) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .build();
            }

            Task createdTask = dataStore.createTask(
                    request.getTitle(),
                    request.getStatus(),
                    request.getUserId()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdTask);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable int id,
            @RequestBody Task request
    ) {
        try {
            Task existingTask = dataStore.getTaskById(id);

            // 404 if task not found
            if (existingTask == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Validate status if provided
            if (request.getStatus() != null &&
                    !isValidStatus(request.getStatus())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // Validate userId if provided
            if (request.getUserId() != 0 &&
                    !dataStore.userExists(request.getUserId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            Task updatedTask = dataStore.updateTask(
                    id,
                    request.getTitle(),
                    request.getStatus(),
                    request.getUserId() == 0 ? null : request.getUserId()
            );

            return ResponseEntity.ok(updatedTask);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // helper methods
    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isValidStatus(String status) {
        return status.equals("pending")
                || status.equals("in-progress")
                || status.equals("completed");
    }

}