package org.example.usermanagement.controller;

import jakarta.validation.Valid;
import org.example.usermanagement.model.User;
import org.example.usermanagement.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operator")
@PreAuthorize("hasRole('OPERATOR')")
public class OperatorController {

    private final UserService userService;

    public OperatorController(UserService userService) {
        this.userService = userService;
    }

    // Получить список всех пользователей (только обычных пользователей)
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        // Оператор может видеть только обычных пользователей, не операторов и не админов
        List<User> filteredUsers = users.stream()
                .filter(user -> user.getRoles().stream()
                        .allMatch(role -> role.equals(org.example.usermanagement.model.RoleName.ROLE_USER)))
                .toList();
        return ResponseEntity.ok(filteredUsers);
    }

    // Получить пользователя по ID
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        // Проверяем, что это обычный пользователь
        if (user.getRoles().stream().anyMatch(role -> !role.equals(org.example.usermanagement.model.RoleName.ROLE_USER))) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        return ResponseEntity.ok(user);
    }

    // Создать нового обычного пользователя
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody @Valid User user) {
        // Оператор может создавать только обычных пользователей
        user.setRoles(java.util.Set.of(org.example.usermanagement.model.RoleName.ROLE_USER));
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    // Обновить пользователя
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        User user = userService.findById(id);
        // Проверяем, что это обычный пользователь
        if (user.getRoles().stream().anyMatch(role -> !role.equals(org.example.usermanagement.model.RoleName.ROLE_USER))) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        // Оператор не может изменять роли пользователей
        userDetails.setRoles(null);

        User updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    // Активировать пользователя
    @PostMapping("/users/{id}/activate")
    public ResponseEntity<String> activateUser(@PathVariable Long id) {
        User user = userService.findById(id);
        // Проверяем, что это обычный пользователь
        if (user.getRoles().stream().anyMatch(role -> !role.equals(org.example.usermanagement.model.RoleName.ROLE_USER))) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        userService.activateUser(id);
        return ResponseEntity.ok("Пользователь успешно активирован");
    }

    // Деактивировать пользователя
    @PostMapping("/users/{id}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable Long id) {
        User user = userService.findById(id);
        // Проверяем, что это обычный пользователь
        if (user.getRoles().stream().anyMatch(role -> !role.equals(org.example.usermanagement.model.RoleName.ROLE_USER))) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        userService.deactivateUser(id);
        return ResponseEntity.ok("Пользователь успешно деактивирован");
    }
}