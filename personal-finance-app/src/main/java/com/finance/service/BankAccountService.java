package com.finance.service;

import com.finance.entity.BankAccount;

import java.util.List;

public interface BankAccountService {

    BankAccount addBankAccount(Long userId,
                               String bankName,
                               String accountHolderName,
                               String accountNumber,
                               String ifscCode,
                               String accountType);

    List<BankAccount> getUserBankAccounts(Long userId);

    boolean verifyBankAccount(Long userId, Long accountId, String otp);
}
