package org.example.usermanagement.service;

import org.example.usermanagement.exception.ResourceNotFoundException;
import org.example.usermanagement.model.Account;
import org.example.usermanagement.model.AccountType;
import org.example.usermanagement.model.User;
import org.example.usermanagement.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserService userService;

    @Autowired
    public AccountService(AccountRepository accountRepository, UserService userService) {
        this.accountRepository = accountRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<Account> findAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Account findAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Счет не найден с ID: " + accountId));
    }

    @Transactional(readOnly = true)
    public Account findAccountByNumber(String accountNumber, Long userId) {
        return accountRepository.findByAccountNumberAndUserId(accountNumber, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Счет не найден с номером: " + accountNumber));
    }

    @Transactional
    public Account createAccount(Long userId, String accountName, AccountType accountType, BigDecimal initialBalance) {
        User user = userService.findById(userId);

        // Generate a unique account number
        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountName(accountName)
                .accountType(accountType)
                .balance(initialBalance != null ? initialBalance : BigDecimal.ZERO)
                .isActive(true)
                .user(user)
                .isExternal(false)
                .build();

        return accountRepository.save(account);
    }

    private String generateAccountNumber() {
        // Generate random account number
        String randomPart = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
        return "ACC" + randomPart;
    }

    @Transactional
    public Account updateAccount(Long accountId, String accountName, AccountType accountType) {
        Account account = findAccountById(accountId);

        if (accountName != null && !accountName.isEmpty()) {
            account.setAccountName(accountName);
        }

        if (accountType != null) {
            account.setAccountType(accountType);
        }

        return accountRepository.save(account);
    }

    @Transactional
    public void deleteAccount(Long accountId) {
        Account account = findAccountById(accountId);
        accountRepository.delete(account);
    }

    @Transactional
    public void deactivateAccount(Long accountId) {
        Account account = findAccountById(accountId);
        account.setIsActive(false);
        accountRepository.save(account);
    }

    @Transactional
    public void activateAccount(Long accountId) {
        Account account = findAccountById(accountId);
        account.setIsActive(true);
        accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalBalance(Long userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);
        return accounts.stream()
                .filter(account -> account.getIsActive() != null && account.getIsActive())
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void updateBalance(Long accountId, BigDecimal newBalance) {
        Account account = findAccountById(accountId);
        account.setBalance(newBalance);
        accountRepository.save(account);
    }

    @Transactional
    public Account linkExternalAccount(Long userId, String plaidAccessToken, String plaidAccountId,
                                       String accountName, String accountNumber, BigDecimal balance, String bankName) {
        User user = userService.findById(userId);

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountName(accountName)
                .accountType(AccountType.EXTERNAL)
                .balance(balance)
                .isActive(true)
                .user(user)
                .isExternal(true)
                .plaidAccessToken(plaidAccessToken)
                .plaidAccountId(plaidAccountId)
                .bankName(bankName)
                .build();

        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public List<Account> findExternalAccountsByUserId(Long userId) {
        return accountRepository.findByUserIdAndIsExternalTrue(userId);
    }

    @Transactional(readOnly = true)
    public List<Account> findInternalAccountsByUserId(Long userId) {
        return accountRepository.findByUserIdAndIsExternalFalse(userId);
    }
}
