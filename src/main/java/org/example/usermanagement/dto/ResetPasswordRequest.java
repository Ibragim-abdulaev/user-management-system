package org.example.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Токен обязателен")
    private String token;

    @NotBlank(message = "Новый пароль обязателен")
    private String newPassword;
}
