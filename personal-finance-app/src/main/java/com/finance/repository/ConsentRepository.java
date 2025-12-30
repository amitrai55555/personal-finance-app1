package com.finance.repository;

import com.finance.entity.Consent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsentRepository extends JpaRepository<Consent, Long> {
    Optional<Consent> findByBankAccountIdAndStatus(
            Long bankAccountId, Consent.ConsentStatus status);
}
