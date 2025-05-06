package org.example.usermanagement.controller;

import jakarta.validation.Valid;
import org.example.usermanagement.dto.LoginRequest;
import org.example.usermanagement.dto.RegisterRequest;
import org.example.usermanagement.dto.ResetPasswordRequest;
import org.example.usermanagement.model.RoleName;
import org.example.usermanagement.model.User;
import org.example.usermanagement.security.JwtTokenProvider;
import org.example.usermanagement.service.EmailService;
import org.example.usermanagement.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwtProvider;
    private final UserService userService;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

    public AuthController(AuthenticationManager authManager, JwtTokenProvider jwtProvider, UserService userService, PasswordEncoder encoder, EmailService emailService) {
        this.authManager = authManager;
        this.jwtProvider = jwtProvider;
        this.userService = userService;
        this.encoder = encoder;
        this.emailService = emailService;
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
        var auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        String token = jwtProvider.generateToken(auth);
        return ResponseEntity.ok(Map.of("token", token));
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
}