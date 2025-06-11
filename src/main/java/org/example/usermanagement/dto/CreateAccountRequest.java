package org.example.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import org.example.usermanagement.model.AccountType;

import java.math.BigDecimal;

@Data
public class CreateAccountRequest {

    @NotBlank(message = "Название счета обязательно")
    private String accountName;

    @NotNull(message = "Тип счета обязателен")
    private AccountType accountType;

    @PositiveOrZero(message = "Начальный баланс должен быть положительным или нулевым")
    private BigDecimal initialBalance;
}