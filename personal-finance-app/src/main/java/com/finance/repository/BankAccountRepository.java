package com.finance.repository;

import com.finance.entity.BankAccount;
import com.finance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    // Get all bank accounts of a user
    List<BankAccount> findByUser(User user);

    // Get only verified bank accounts of a user
    List<BankAccount> findByUserAndIsVerifiedTrue(User user);

    // Find a specific account by ID and user (security)
    Optional<BankAccount> findByIdAndUser(Long id, User user);

    // Prevent duplicate account linking (encrypted value check)
    boolean existsByAccountNumberEncrypted(String accountNumberEncrypted);

    // Optional: find by IFSC if needed
    List<BankAccount> findByIfscCode(String ifscCode);
}
