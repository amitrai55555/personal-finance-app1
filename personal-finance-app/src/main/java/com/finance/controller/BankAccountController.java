package com.finance.controller;

import com.finance.dto.AddBankAccountRequest;
import com.finance.dto.VerifyBankAccountRequest;
import com.finance.entity.BankAccount;
import com.finance.security.UserPrincipal;
import com.finance.service.AccountAggregatorService;
import com.finance.service.BankAccountService;
import com.finance.Util.EncryptionUtil;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bank-account")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BankAccountController {
        private final AccountAggregatorService aaService;
        private final BankAccountService bankAccountService;
        private final EncryptionUtil encryptionUtil;

        public BankAccountController(AccountAggregatorService aaService, BankAccountService bankAccountService,
                        EncryptionUtil encryptionUtil) {
                this.aaService = aaService;
                this.bankAccountService = bankAccountService;
                this.encryptionUtil = encryptionUtil;
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
                                request.getAccountType());

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
                                request.getOtp());

                return ResponseEntity.ok(Map.of(
                                "message", "Bank account verified successfully"));
        }

        @DeleteMapping("/{bankAccountId}")
        public ResponseEntity<?> deleteBankAccount(
                        @PathVariable Long bankAccountId,
                        Authentication authentication) {

                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

                bankAccountService.deleteBankAccount(
                                userPrincipal.getId(),
                                bankAccountId);

                return ResponseEntity.ok(
                                Map.of("message", "Bank account deleted successfully"));
        }

        // ===============================
        // GET USER BANK ACCOUNTS
        // ===============================
        @GetMapping("/my")
        public ResponseEntity<?> getMyBankAccounts(Authentication authentication) {

                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

                List<BankAccount> accounts = bankAccountService.getUserBankAccounts(userPrincipal.getId());

                // Mask account number in response
                List<Map<String, Object>> response = accounts.stream().map(account -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", account.getId());
                        map.put("bankName", account.getBankName());
                        map.put("accountHolderName", account.getAccountHolderName());
                        String decryptedNumber = encryptionUtil.decrypt(account.getAccountNumberEncrypted());
                        String maskedNumber = "X".repeat(Math.max(0, decryptedNumber.length() - 4))
                                        + decryptedNumber.substring(Math.max(0, decryptedNumber.length() - 4));
                        map.put("accountNumber", maskedNumber);
                        map.put("ifscCode", account.getIfscCode());
                        map.put("accountType", account.getAccountType());
                        map.put("verified", account.isVerified());
                        return map;
                }).toList();

                return ResponseEntity.ok(response);
        }

        @PostMapping("/aa/connect/{bankAccountId}")
        public ResponseEntity<?> connectAA(
                        @PathVariable Long bankAccountId,
                        @AuthenticationPrincipal UserPrincipal user) {

                String consentHandle = aaService.initiateConsent(user.getId(), bankAccountId);

                return ResponseEntity.ok(
                                Map.of("consentHandle", consentHandle));
        }

        @PostMapping("/aa/sync/{bankAccountId}")
        public ResponseEntity<?> sync(
                        @PathVariable Long bankAccountId,
                        @AuthenticationPrincipal UserPrincipal user) {

                aaService.syncTransactions(user.getId(), bankAccountId);
                return ResponseEntity.ok("Transactions synced");
        }

        @PostMapping("/{bankAccountId}/delete/request-otp")
        public ResponseEntity<?> requestDeleteOtp(
                        @PathVariable Long bankAccountId,
                        Authentication authentication) {

                UserPrincipal user = (UserPrincipal) authentication.getPrincipal();

                bankAccountService.requestDeleteOtp(
                                user.getId(),
                                bankAccountId);

                return ResponseEntity.ok(
                                Map.of("message", "OTP sent to your registered email"));
        }

        @DeleteMapping("/{bankAccountId}/delete/confirm")
        public ResponseEntity<?> confirmDeleteBankAccount(
                        @PathVariable Long bankAccountId,
                        @RequestBody Map<String, String> body,
                        Authentication authentication) {

                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

                String otp = body.get("otp");

                bankAccountService.deleteBankAccountWithOtp(
                                userPrincipal.getId(),
                                bankAccountId,
                                otp);

                return ResponseEntity.ok(
                                Map.of("message", "Bank account deleted successfully"));
        }

}
