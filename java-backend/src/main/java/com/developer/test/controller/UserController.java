package com.developer.test.controller;

import com.developer.test.dto.UsersResponse;
import com.developer.test.model.User;
import com.developer.test.service.DataStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final DataStore dataStore;

    public UserController(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    //    public List<User> users = new ArrayList<>();
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @GetMapping
    public ResponseEntity<UsersResponse> getUsers() {
        List<User> users = dataStore.getUsers();
        UsersResponse response = new UsersResponse(users, users.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        User user = dataStore.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User request) {
        try {
            // Validate required fields
            if (isNullOrEmpty(request.getName())
                    || isNullOrEmpty(request.getEmail())
                    || isNullOrEmpty(request.getRole())) {

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .build();
            }

            // Validate email format
            if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .build();
            }

//            after db connection
//            int newId = users.stream()
//                    .mapToInt(User::getId)
//                    .max()
//                    .orElse(0) + 1;
//
//            User newUser = new User(
//                    newId,
//                    request.getName(),
//                    request.getEmail(),
//                    request.getRole()
//            );
//
//            users.add(newUser);

            User createdUser = dataStore.createUser(
                    request.getName(),
                    request.getEmail(),
                    request.getRole()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdUser);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    // helper methods
    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

}