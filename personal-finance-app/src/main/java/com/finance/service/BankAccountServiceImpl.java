package com.finance.service;

import com.finance.entity.BankAccount;
import com.finance.entity.User;
import com.finance.repository.BankAccountRepository;
import com.finance.repository.OtpTokenRepository;
import com.finance.repository.UserRepository;
import com.finance.Util.EncryptionUtil;
import com.finance.service.Mail.OtpService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private final OtpService otpService;
    private OtpTokenRepository otpTokenRepository;

    public BankAccountServiceImpl(BankAccountRepository bankAccountRepository,
            UserRepository userRepository,
            EncryptionUtil encryptionUtil,
            OtpService otpService, OtpTokenRepository otpTokenRepository)

    {
        System.out.println("BankAccountServiceImpl");
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
        this.encryptionUtil = encryptionUtil;
        this.otpService = otpService;
        this.otpTokenRepository = otpTokenRepository;
    }

    @Override
    public BankAccount addBankAccount(Long userId,
            String bankName,
            String accountHolderName,
            String accountNumber,
            String ifscCode,
            String accountType) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Encrypt account number
        String encryptedAccountNumber = encryptionUtil.encrypt(accountNumber);

        // Prevent duplicate account
        if (bankAccountRepository.existsByAccountNumberEncrypted(encryptedAccountNumber)) {
            throw new RuntimeException("Bank account already linked");
        }

        BankAccount account = new BankAccount();
        account.setUser(user);
        account.setBankName(bankName);
        account.setAccountHolderName(accountHolderName);
        account.setAccountNumberEncrypted(encryptedAccountNumber);
        account.setIfscCode(ifscCode);
        account.setAccountType(accountType);
        account.setVerified(false);

        BankAccount savedAccount = bankAccountRepository.save(account);

        // 🔐 Generate & send OTP
        otpService.generateAndSendOtp(user, savedAccount);

        return savedAccount;
    }

    @Override
    public List<BankAccount> getUserBankAccounts(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return bankAccountRepository.findByUser(user);
    }

    @Override
    public boolean verifyBankAccount(Long userId, Long accountId, String otp) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BankAccount account = bankAccountRepository
                .findByIdAndUser(accountId, user)
                .orElseThrow(() -> new RuntimeException("Bank account not found"));

        // 🔐 Verify OTP
        boolean valid = otpService.verifyOtp(user, account, otp);

        if (!valid) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        account.setVerified(true);
        bankAccountRepository.save(account);

        return true;
    }

    public void deleteBankAccount(Long userId, Long bankAccountId) {
        System.out.println("otpTokenRepository = " + otpTokenRepository);

        BankAccount account = bankAccountRepository
                .findByIdAndUserId(bankAccountId, userId)
                .orElseThrow(() -> new RuntimeException("Bank account not found"));

        // Allow deletion of both verified and unverified accounts

        // 🔥 delete related OTPs first
        otpTokenRepository.deleteByBankAccount(account);

        // 🔥 delete bank account
        bankAccountRepository.delete(account);
    }

    public void requestDeleteOtp(Long userId, Long bankAccountId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BankAccount account = bankAccountRepository
                .findByIdAndUserId(bankAccountId, userId)
                .orElseThrow(() -> new RuntimeException("Bank account not found"));

        // Allow deletion of both verified and unverified accounts
        // OTP is still required for security

        // 🔐 Generate & send OTP (reuse existing OTP system)
        otpService.generateAndSendOtp(user, account);
    }

    public void deleteBankAccountWithOtp(
            Long userId,
            Long bankAccountId,
            String otp) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BankAccount account = bankAccountRepository
                .findByIdAndUserId(bankAccountId, userId)
                .orElseThrow(() -> new RuntimeException("Bank account not found"));

        // 🔐 VERIFY OTP
        boolean valid = otpService.verifyOtp(user, account, otp);

        if (!valid) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        // 🔥 DELETE RELATED OTPs (FK SAFE)
        otpTokenRepository.deleteByBankAccount(account);

        // 🔥 DELETE BANK ACCOUNT
        bankAccountRepository.delete(account);
    }

}
