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
    private final EmailService emailService;

    public OtpServiceImpl(OtpTokenRepository otpTokenRepository,
                          EmailService emailService) {
        this.otpTokenRepository = otpTokenRepository;
        this.emailService = emailService;
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

        // 3️⃣ Send OTP via Email
        emailService.sendOtpVerificationEmail(user, otp);
        System.out.println("hit2");

    }

    @Override
    public boolean verifyOtp(User user, BankAccount bankAccount, String otp) {

        OtpToken token = otpTokenRepository
                .findTopByUserAndBankAccountAndUsedFalseOrderByCreatedAtDesc(user, bankAccount)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        // Check expiry
        if (token.isExpired()) {
            throw new RuntimeException("OTP expired");
        }

        // Check OTP match
        if (!token.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        // Mark as used
        token.setUsed(true);
        otpTokenRepository.save(token);

        return true;
    }

    private String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }
}
