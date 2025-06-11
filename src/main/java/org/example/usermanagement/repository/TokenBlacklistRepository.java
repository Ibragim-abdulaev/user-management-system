package org.example.usermanagement.repository;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

/**
 * Репозиторий для отозванных токенов (блэклист)
 * В продакшене лучше использовать Redis или другое распределенное хранилище
 */
@Repository
public class TokenBlacklistRepository {

    // В реальном приложении использовать Redis или другое распределенное хранилище
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    private final Set<String> blacklistedRefreshTokens = ConcurrentHashMap.newKeySet();

    /**
     * Добавляет токен в черный список
     */
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }

    /**
     * Добавляет refresh-токен в черный список
     */
    public void blacklistRefreshToken(String refreshToken) {
        blacklistedRefreshTokens.add(refreshToken);
    }

    /**
     * Проверяет, находится ли токен в черном списке
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    /**
     * Проверяет, находится ли refresh-токен в черном списке
     */
    public boolean isRefreshTokenBlacklisted(String refreshToken) {
        return blacklistedRefreshTokens.contains(refreshToken);
    }
}