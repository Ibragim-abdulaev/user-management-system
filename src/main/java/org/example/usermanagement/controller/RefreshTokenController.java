
package org.example.usermanagement.controller;

import org.example.usermanagement.model.User;
import org.example.usermanagement.security.JwtTokenProvider;
import org.example.usermanagement.service.CustomUserDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class RefreshTokenController {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public RefreshTokenController(JwtTokenProvider tokenProvider, CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        // Проверяем валидность refresh токена
        if (refreshToken != null && tokenProvider.validateToken(refreshToken)) {
            // Извлекаем email пользователя из токена
            String email = tokenProvider.getEmailFromToken(refreshToken);

            // Загружаем данные пользователя
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // Создаем новый объект Authentication
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            // Генерируем новую пару токенов
            String newAccessToken = tokenProvider.generateToken(authentication);
            String newRefreshToken = tokenProvider.generateRefreshToken(authentication);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", newAccessToken);
            tokens.put("refreshToken", newRefreshToken);

            return ResponseEntity.ok(tokens);
        }

        return ResponseEntity.badRequest().body(Map.of("error", "Invalid refresh token"));
    }
}