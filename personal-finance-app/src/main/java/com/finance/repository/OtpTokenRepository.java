package com.finance.repository;

import com.finance.entity.BankAccount;
import com.finance.entity.OtpToken;
import com.finance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    // Get latest unused OTP for user & account
    Optional<OtpToken> findTopByUserAndBankAccountAndUsedFalseOrderByCreatedAtDesc(
            User user,
            BankAccount bankAccount
    );

    // Optional cleanup: delete expired OTPs
    void deleteByExpiryTimeBefore(java.time.LocalDateTime time);
}
