package com.finance.service;

import com.finance.entity.BankAccount;
import com.finance.entity.User;

public interface OtpService {

    void generateAndSendOtp(User user, BankAccount bankAccount);

    boolean verifyOtp(User user, BankAccount bankAccount, String otp);
}
