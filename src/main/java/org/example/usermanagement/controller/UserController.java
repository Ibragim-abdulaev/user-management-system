package org.example.usermanagement.controller;

import jakarta.validation.Valid;
import org.example.usermanagement.model.User;
import org.example.usermanagement.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Получить профиль текущего пользователя
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();
        User user = userService.findByEmail(currentEmail);
        return ResponseEntity.ok(user);
    }

    // Обновить данные профиля текущего пользователя
    @PutMapping("/me")
    public ResponseEntity<User> updateCurrentUser(@RequestBody @Valid User userDetails) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();
        User currentUser = userService.findByEmail(currentEmail);

        // Обновляем только разрешенные поля (без изменения ролей)
        User updatedUser = userService.updateUser(currentUser.getId(), userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    // Изменить пароль для текущего пользователя
    @PostMapping("/me/change-password")
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> passwordData) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();

        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");

        userService.changePassword(currentEmail, currentPassword, newPassword);
        return ResponseEntity.ok("Пароль успешно изменен");
    }

    // Деактивировать свой аккаунт
    @PostMapping("/me/deactivate")
    public ResponseEntity<String> deactivateAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();
        User currentUser = userService.findByEmail(currentEmail);

        userService.deactivateUser(currentUser.getId());
        return ResponseEntity.ok("Аккаунт успешно деактивирован");
    }
}

// Вспомогательный класс для изменения пароля
class Map<K, V> extends HashMap<K, V> {
}