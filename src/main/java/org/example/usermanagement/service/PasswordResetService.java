package org.example.usermanagement.service;

import org.example.usermanagement.model.User;
import org.example.usermanagement.repository.UserRepository;
import org.example.usermanagement.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public PasswordResetService(UserRepository userRepository, JavaMailSender mailSender, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // Запрос на сброс пароля
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь с таким email не найден"));

        // Генерация уникального токена сброса
        String resetToken = UUID.randomUUID().toString();

        // Сохраняем токен в базе данных
        user.setResetToken(resetToken);
        userRepository.save(user);

        // Отправляем email с ссылкой для сброса пароля
        sendPasswordResetEmail(user.getEmail(), resetToken);
    }

    // Отправка email
    private void sendPasswordResetEmail(String email, String resetToken) {
        String resetUrl = "http://localhost:8080/api/auth/reset-password?token=" + resetToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Сброс пароля");
        message.setText("Чтобы сбросить пароль, перейдите по следующей ссылке: " + resetUrl);
        mailSender.send(message);
    }

    // Подтверждение сброса пароля
    public void resetPassword(String resetToken, String newPassword) {
        User user = userRepository.findByResetToken(resetToken)
                .orElseThrow(() -> new RuntimeException("Неверный или истекший токен сброса"));

        // Кодируем новый пароль
        user.setPassword(newPassword); // Примените здесь кодировку пароля (например, BCrypt)
        user.setResetToken(null); // Очистим токен сброса после использования
        userRepository.save(user);
    }
}
