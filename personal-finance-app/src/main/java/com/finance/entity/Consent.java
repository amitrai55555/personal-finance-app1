package com.finance.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "consents")
public class Consent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long bankAccountId;

    private String consentHandle;   // from AA
    private String consentId;       // after approval

    @Enumerated(EnumType.STRING)
    private ConsentStatus status;

    private LocalDate validTill;

    public enum ConsentStatus {
        PENDING,
        ACTIVE,
        REVOKED,
        EXPIRED
    }
}
