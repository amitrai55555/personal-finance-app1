package com.finance.dto;

import jakarta.validation.constraints.NotBlank;

public class AddBankAccountRequest {

    @NotBlank
    private String bankName;

    @NotBlank
    private String accountHolderName;

    @NotBlank
    private String accountNumber;

    @NotBlank
    private String ifscCode;

    private String accountType;

    // getters & setters
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
}
