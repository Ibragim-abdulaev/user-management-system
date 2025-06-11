package org.example.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.usermanagement.model.AccountType;

import java.math.BigDecimal;

@Data
public class AccountDTO {

    private Long id;

    private String accountNumber;

    @NotBlank(message = "Название счета обязательно")
    private String accountName;

    private BigDecimal balance;

    @NotNull(message = "Тип счета обязателен")
    private AccountType accountType;

    private Boolean isActive;

    private Boolean isExternal;

    private String bankName;
}