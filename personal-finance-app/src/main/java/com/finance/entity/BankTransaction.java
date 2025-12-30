package com.finance.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "bank_transactions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"referenceId"})
        }
)

public class BankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔑 Link to User (MANDATORY)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 🔑 Link to BankAccount entity (MANDATORY)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    // 🔑 Unique reference from bank / AA
    @Column(nullable = false, unique = true)
    private String referenceId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TxnType type; // CREDIT / DEBIT

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate txnDate;

    // Optional but REALISTIC
    private String currency = "INR";

    private BigDecimal balanceAfterTxn;


    public enum TxnType {
        CREDIT,
        DEBIT
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setType(TxnType type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTxnDate(LocalDate txnDate) {
        this.txnDate = txnDate;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setBalanceAfterTxn(BigDecimal balanceAfterTxn) {
        this.balanceAfterTxn = balanceAfterTxn;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TxnType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getTxnDate() {
        return txnDate;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalanceAfterTxn() {
        return balanceAfterTxn;
    }

    private BankTransaction createTxn(
            User user,
            BankAccount bankAccount,
            int amt,
            String desc,
            BankTransaction.TxnType type,
            String referenceId
    )

    {
        BankTransaction t = new BankTransaction();

        t.setUser(user);                     // 🔑 REQUIRED
        t.setBankAccount(bankAccount);       // 🔑 REQUIRED
        t.setAmount(BigDecimal.valueOf(amt));
        t.setDescription(desc);
        t.setTxnDate(LocalDate.now());
        t.setType(type);
        t.setReferenceId(referenceId);       // 🔑 REQUIRED (unique)

        return t;
    }
}