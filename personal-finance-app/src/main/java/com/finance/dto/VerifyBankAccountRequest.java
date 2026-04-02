package com.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class VerifyBankAccountRequest {

    @NotNull
    private Long accountId;

    @NotBlank
    private String otp;

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}
