package org.example.usermanagement.repository;

import org.example.usermanagement.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserId(Long userId);

    Optional<Account> findByAccountNumberAndUserId(String accountNumber, Long userId);

    // Find external accounts linked to a user
    List<Account> findByUserIdAndIsExternalTrue(Long userId);

    // Find internal accounts for a user
    List<Account> findByUserIdAndIsExternalFalse(Long userId);

    boolean existsByAccountNumberAndUserId(String accountNumber, Long userId);

    boolean existsByPlaidAccountIdAndUserId(String plaidAccountId, Long userId);
}