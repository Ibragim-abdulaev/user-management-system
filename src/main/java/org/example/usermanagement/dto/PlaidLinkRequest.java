package org.example.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlaidLinkRequest {

    @NotBlank(message = "Публичный токен обязателен")
    private String publicToken;
}