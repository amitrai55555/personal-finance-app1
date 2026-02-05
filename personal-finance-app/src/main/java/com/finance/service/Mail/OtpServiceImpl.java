package com.finance.service.Mail;

import com.finance.entity.BankAccount;
import com.finance.entity.OtpToken;
import com.finance.entity.User;
import com.finance.repository.OtpTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@Transactional
public class OtpServiceImpl implements OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final OtpEmailService otpEmailService;

    public OtpServiceImpl(OtpTokenRepository otpTokenRepository,
                          OtpEmailService otpEmailService) {
        this.otpTokenRepository = otpTokenRepository;
        this.otpEmailService = otpEmailService;
    }

    @Override
    public void generateAndSendOtp(User user, BankAccount bankAccount) {
        String otp = generateOtp();

        OtpToken token = new OtpToken();
        token.setUser(user);
        token.setBankAccount(bankAccount);
        token.setOtp(otp);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        token.setUsed(false);

        otpTokenRepository.save(token);

        // Send OTP via email
        otpEmailService.sendOtpVerificationEmail(user, otp);
    }

    @Override
    public boolean verifyOtp(User user, BankAccount bankAccount, String otp) {
        OtpToken token = otpTokenRepository
                .findTopByUserAndBankAccountAndUsedFalseOrderByCreatedAtDesc(user, bankAccount)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (token.isExpired()) {
            throw new RuntimeException("OTP expired");
        }

        if (!token.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        token.setUsed(true);
        otpTokenRepository.save(token);

        return true;
    }

    private String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }
}
