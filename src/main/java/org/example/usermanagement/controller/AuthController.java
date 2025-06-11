package org.example.usermanagement.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.usermanagement.dto.LoginRequest;
import org.example.usermanagement.dto.RegisterRequest;
import org.example.usermanagement.dto.ResetPasswordRequest;
import org.example.usermanagement.model.RoleName;
import org.example.usermanagement.model.User;
import org.example.usermanagement.repository.TokenBlacklistRepository;
import org.example.usermanagement.security.JwtTokenProvider;
import org.example.usermanagement.service.EmailService;
import org.example.usermanagement.service.TokenService;
import org.example.usermanagement.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwtProvider;
    private final UserService userService;
    private final TokenService tokenService;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

    private final TokenBlacklistRepository tokenBlacklistRepository;

    public AuthController(AuthenticationManager authManager, JwtTokenProvider jwtProvider, UserService userService, TokenService tokenService, PasswordEncoder encoder, EmailService emailService, TokenBlacklistRepository tokenBlacklistRepository) {
        this.authManager = authManager;
        this.jwtProvider = jwtProvider;
        this.userService = userService;
        this.tokenService = tokenService;
        this.encoder = encoder;
        this.emailService = emailService;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email уже используется");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .roles(Set.of(RoleName.ROLE_USER))
                .build();

        userService.save(user);
        return ResponseEntity.ok("Пользователь зарегистрирован");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody @Valid LoginRequest request) {
        // Проверяем, есть ли уже действующий токен для данного пользователя
        String email = request.getEmail();
        String existingToken = tokenService.getActiveTokenForUser(email);

        if (existingToken != null && jwtProvider.validateToken(existingToken)) {
            // Если токен существует и действителен, возвращаем его
            return ResponseEntity.ok(Map.of(
                    "token", existingToken,
                    "message", "Используется существующая сессия"
            ));
        }

        // Если действующего токена нет, выполняем обычную аутентификацию
        var auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        // Генерируем новый токен
        String newToken = jwtProvider.generateToken(auth);

        // Сохраняем этот токен в сервисе
        tokenService.saveTokenForUser(email, newToken);

        return ResponseEntity.ok(Map.of("token", newToken));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        if (!userService.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("Пользователь с таким email не найден");
        }

        String resetToken = userService.generateResetToken(email);
        emailService.sendPasswordResetEmail(email, resetToken);
        return ResponseEntity.ok("Инструкции по сбросу пароля отправлены на ваш email");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        if (!userService.isResetTokenValid(request.getToken())) {
            return ResponseEntity.badRequest().body("Неверный или истекший токен сброса пароля");
        }

        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Пароль успешно изменен");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        // Получаем токен из запроса
        String jwt = getJwtFromRequest(request);

        // Если токен есть, добавляем его в черный список
        if (StringUtils.hasText(jwt)) {
            tokenBlacklistRepository.blacklistToken(jwt);
        }

        // Очищаем контекст безопасности
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Выход выполнен успешно");
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}