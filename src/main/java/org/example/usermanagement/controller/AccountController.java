package org.example.usermanagement.controller;

import jakarta.validation.Valid;
import org.example.usermanagement.dto.AccountDTO;
import org.example.usermanagement.dto.CreateAccountRequest;
import org.example.usermanagement.model.Account;
import org.example.usermanagement.model.User;
import org.example.usermanagement.service.AccountService;
import org.example.usermanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;

    @Autowired
    public AccountController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    // Получить список всех счетов текущего пользователя
    @GetMapping
    public ResponseEntity<List<AccountDTO>> getCurrentUserAccounts() {
        Long userId = getCurrentUserId();
        List<Account> accounts = accountService.findAccountsByUserId(userId);
        List<AccountDTO> accountDTOs = accounts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accountDTOs);
    }

    // Получить счет по ID
    @GetMapping("/{id}")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Account account = accountService.findAccountById(id);

        // Проверяем, что счет принадлежит текущему пользователю
        if (!account.getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        return ResponseEntity.ok(convertToDTO(account));
    }

    // Создать новый счет
    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Long userId = getCurrentUserId();

        Account account = accountService.createAccount(
                userId,
                request.getAccountName(),
                request.getAccountType(),
                request.getInitialBalance()
        );

        return ResponseEntity.ok(convertToDTO(account));
    }

    // Обновить счет
    @PutMapping("/{id}")
    public ResponseEntity<AccountDTO> updateAccount(@PathVariable Long id, @RequestBody AccountDTO accountDTO) {
        Long userId = getCurrentUserId();
        Account account = accountService.findAccountById(id);

        // Проверяем, что счет принадлежит текущему пользователю
        if (!account.getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        account = accountService.updateAccount(id, accountDTO.getAccountName(), accountDTO.getAccountType());
        return ResponseEntity.ok(convertToDTO(account));
    }

    // Удалить счет
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAccount(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Account account = accountService.findAccountById(id);

        // Проверяем, что счет принадлежит текущему пользователю
        if (!account.getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        accountService.deleteAccount(id);
        return ResponseEntity.ok("Счет успешно удален");
    }

    // Деактивировать счет
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivateAccount(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Account account = accountService.findAccountById(id);

        // Проверяем, что счет принадлежит текущему пользователю
        if (!account.getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        accountService.deactivateAccount(id);
        return ResponseEntity.ok("Счет успешно деактивирован");
    }

    // Активировать счет
    @PostMapping("/{id}/activate")
    public ResponseEntity<String> activateAccount(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Account account = accountService.findAccountById(id);

        // Проверяем, что счет принадлежит текущему пользователю
        if (!account.getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        accountService.activateAccount(id);
        return ResponseEntity.ok("Счет успешно активирован");
    }

    // Получить общий баланс пользователя
    @GetMapping("/balance")
    public ResponseEntity<Map<String, BigDecimal>> getTotalBalance() {
        Long userId = getCurrentUserId();
        BigDecimal totalBalance = accountService.getTotalBalance(userId);
        return ResponseEntity.ok(Map.of("totalBalance", totalBalance));
    }

    // Получить внешние счета пользователя (связанные через Plaid)
    @GetMapping("/external")
    public ResponseEntity<List<AccountDTO>> getExternalAccounts() {
        Long userId = getCurrentUserId();
        List<Account> externalAccounts = accountService.findExternalAccountsByUserId(userId);
        List<AccountDTO> accountDTOs = externalAccounts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accountDTOs);
    }

    // Вспомогательный метод для получения ID текущего пользователя
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();
        User user = userService.findByEmail(currentEmail);
        return user.getId();
    }

    // Преобразование Account в AccountDTO
    private AccountDTO convertToDTO(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setAccountName(account.getAccountName());
        dto.setBalance(account.getBalance());
        dto.setAccountType(account.getAccountType());
        dto.setIsActive(account.getIsActive());
        dto.setIsExternal(account.getIsExternal());
        dto.setBankName(account.getBankName());
        return dto;
    }
}