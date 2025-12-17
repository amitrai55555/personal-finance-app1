package com.finance.controller;

import com.finance.dto.AddBankAccountRequest;
import com.finance.dto.VerifyBankAccountRequest;
import com.finance.entity.BankAccount;
import com.finance.security.UserPrincipal;
import com.finance.service.BankAccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bank-account")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    // ===============================
    // ADD BANK ACCOUNT
    // ===============================
    @PostMapping("/add")
    public ResponseEntity<?> addBankAccount(
            @Valid @RequestBody AddBankAccountRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        BankAccount account = bankAccountService.addBankAccount(
                userPrincipal.getId(),
                request.getBankName(),
                request.getAccountHolderName(),
                request.getAccountNumber(),
                request.getIfscCode(),
                request.getAccountType()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Bank account added. OTP sent for verification.");
        response.put("accountId", account.getId());
        response.put("verified", account.isVerified());

        return ResponseEntity.ok(response);
    }

    // ===============================
    // VERIFY BANK ACCOUNT (OTP)
    // ===============================
    @PostMapping("/verify")
    public ResponseEntity<?> verifyBankAccount(
            @Valid @RequestBody VerifyBankAccountRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        bankAccountService.verifyBankAccount(
                userPrincipal.getId(),
                request.getAccountId(),
                request.getOtp()
        );

        return ResponseEntity.ok(Map.of(
                "message", "Bank account verified successfully"
        ));
    }

    // ===============================
    // GET USER BANK ACCOUNTS
    // ===============================
    @GetMapping("/my")
    public ResponseEntity<?> getMyBankAccounts(Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        List<BankAccount> accounts =
                bankAccountService.getUserBankAccounts(userPrincipal.getId());

        // Mask account number in response
        List<Map<String, Object>> response = accounts.stream().map(account -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", account.getId());
            map.put("bankName", account.getBankName());
            map.put("accountHolderName", account.getAccountHolderName());
            map.put("accountNumber", "XXXXXX" +
                    account.getAccountNumberEncrypted()
                            .substring(account.getAccountNumberEncrypted().length() - 4));
            map.put("ifscCode", account.getIfscCode());
            map.put("accountType", account.getAccountType());
            map.put("verified", account.isVerified());
            return map;
        }).toList();

        return ResponseEntity.ok(response);
    }
}
