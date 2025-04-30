package org.example.usermanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Сервис для аудита действий безопасности в системе
 * Важно логировать все критические действия, связанные с безопасностью
 */
@Service
public class SecurityAuditService {
    private static final Logger logger = LoggerFactory.getLogger(SecurityAuditService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Логирует попытку входа пользователя
     */
    public void logLoginAttempt(String email, boolean success, String ipAddress) {
        String message = success
                ? String.format("LOGIN_SUCCESS: Пользователь %s успешно вошел в систему с IP %s", email, ipAddress)
                : String.format("LOGIN_FAILURE: Неудачная попытка входа для пользователя %s с IP %s", email, ipAddress);

        logger.info(message);
    }

    /**
     * Логирует изменение пароля пользователя
     */
    public void logPasswordChange(String email, String initiatedBy) {
        String timestamp = LocalDateTime.now().format(formatter);
        logger.info("PASSWORD_CHANGE: Пароль для пользователя {} был изменен пользователем {} в {}",
                email, initiatedBy, timestamp);
    }

    /**
     * Логирует изменение ролей пользователя
     */
    public void logRoleChange(String targetUser, String changedBy, String previousRoles, String newRoles) {
        String timestamp = LocalDateTime.now().format(formatter);
        logger.info("ROLE_CHANGE: Роли пользователя {} изменены пользователем {} в {}. Было: {}. Стало: {}",
                targetUser, changedBy, timestamp, previousRoles, newRoles);
    }

    /**
     * Логирует запрос на сброс пароля
     */
    public void logPasswordResetRequest(String email, String ipAddress) {
        String timestamp = LocalDateTime.now().format(formatter);
        logger.info("PASSWORD_RESET_REQUEST: Запрос на сброс пароля для {} с IP {} в {}",
                email, ipAddress, timestamp);
    }

    /**
     * Логирует выполнение сброса пароля
     */
    public void logPasswordReset(String email) {
        String timestamp = LocalDateTime.now().format(formatter);
        logger.info("PASSWORD_RESET_COMPLETE: Сброс пароля для {} выполнен в {}", email, timestamp);
    }

    /**
     * Логирует деактивацию учетной записи
     */
    public void logAccountDeactivation(String targetUser, String deactivatedBy) {
        String timestamp = LocalDateTime.now().format(formatter);
        logger.info("ACCOUNT_DEACTIVATION: Аккаунт пользователя {} деактивирован пользователем {} в {}",
                targetUser, deactivatedBy, timestamp);
    }

    /**
     * Логирует активацию учетной записи
     */
    public void logAccountActivation(String targetUser, String activatedBy) {
        String timestamp = LocalDateTime.now().format(formatter);
        logger.info("ACCOUNT_ACTIVATION: Аккаунт пользователя {} активирован пользователем {} в {}",
                targetUser, activatedBy, timestamp);
    }

    /**
     * Логирует отказ в доступе
     */
    public void logAccessDenied(String email, String resource, String ipAddress) {
        String timestamp = LocalDateTime.now().format(formatter);
        logger.warn("ACCESS_DENIED: Отказ в доступе для пользователя {} к ресурсу {} с IP {} в {}",
                email, resource, ipAddress, timestamp);
    }
}