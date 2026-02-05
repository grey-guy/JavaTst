
        package com.developer.test.service;

import com.developer.test.model.Task;
import com.developer.test.model.User;
import com.developer.test.dto.StatsResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class DataStore {
    private final ConcurrentHashMap<Integer, User> users = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicInteger nextUserId = new AtomicInteger(1);
    private final AtomicInteger nextTaskId = new AtomicInteger(1);

    public DataStore() {
        // Initialize with sample data
        users.put(1, new User(1, "John Doe", "john@example.com", "developer"));
        users.put(2, new User(2, "Jane Smith", "jane@example.com", "designer"));
        users.put(3, new User(3, "Bob Johnson", "bob@example.com", "manager"));

        tasks.put(1, new Task(1, "Implement authentication", "pending", 1));
        tasks.put(2, new Task(2, "Design user interface", "in-progress", 2));
        tasks.put(3, new Task(3, "Review code changes", "completed", 3));

        nextUserId.set(4);
        nextTaskId.set(4);
    }

    // ------- USERS -------
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    public User getUserById(int id) {
        return users.get(id);
    }

    public User createUser(String name, String email, String role) {
        int id = nextUserId.getAndIncrement();
        User user = new User(id, name, email, role);
        users.put(id, user);
        return user;
    }

    // ------- TASKS -------
    public List<Task> getTasks(String status, String userId) {
        List<Task> allTasks = new ArrayList<>(tasks.values());

        return allTasks.stream()
                .filter(task -> {
                    boolean matchStatus = status == null || status.isEmpty() || task.getStatus().equals(status);
                    boolean matchUserId = userId == null || userId.isEmpty() ||
                            task.getUserId() == Integer.parseInt(userId);
                    return matchStatus && matchUserId;
                })
                .collect(Collectors.toList());
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public boolean userExists(int userId) {
        return users.containsKey(userId);
    }

    public Task createTask(String title, String status, int userId) {
        int id = nextTaskId.getAndIncrement();
        Task task = new Task(id, title, status, userId);
        tasks.put(id, task);
        return task;
    }

    public Task updateTask(
            int taskId,
            String title,
            String status,
            Integer userId
    ) {
        Task existingTask = tasks.get(taskId);

        if (existingTask == null) {
            return null;
        }

        // Partial updates
        if (title != null) {
            existingTask.setTitle(title);
        }

        if (status != null) {
            existingTask.setStatus(status);
        }

        if (userId != null) {
            existingTask.setUserId(userId);
        }

        tasks.put(taskId, existingTask);
        return existingTask;
    }


    // ------- STATS -------
    public StatsResponse getStats() {
        StatsResponse stats = new StatsResponse();
        stats.getUsers().setTotal(users.size());
        stats.getTasks().setTotal(tasks.size());

        for (Task task : tasks.values()) {
            switch (task.getStatus()) {
                case "pending":
                    stats.getTasks().setPending(stats.getTasks().getPending() + 1);
                    break;
                case "in-progress":
                    stats.getTasks().setInProgress(stats.getTasks().getInProgress() + 1);
                    break;
                case "completed":
                    stats.getTasks().setCompleted(stats.getTasks().getCompleted() + 1);
                    break;
            }
        }

        return stats;
    }
}