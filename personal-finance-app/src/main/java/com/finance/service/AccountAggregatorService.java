package com.finance.service;

import com.finance.dto.ExpenseRequest;
import com.finance.dto.IncomeRequest;
import com.finance.entity.*;
import com.finance.repository.BankAccountRepository;
import com.finance.repository.BankTransactionRepository;
import com.finance.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AccountAggregatorService {

    private final BankTransactionRepository txnRepo;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final UserRepository userRepo;
    private final BankAccountRepository bankAccountRepo;

    public AccountAggregatorService(
            BankTransactionRepository txnRepo,
            IncomeService incomeService,
            ExpenseService expenseService,
            UserRepository userRepo,
            BankAccountRepository bankAccountRepo
    ) {
        this.txnRepo = txnRepo;
        this.incomeService = incomeService;
        this.expenseService = expenseService;
        this.userRepo = userRepo;
        this.bankAccountRepo = bankAccountRepo;
    }

    // 🔹 Step 1: Create Consent (Sandbox)
    public String initiateConsent(Long userId, Long bankAccountId) {
        return "CONSENT-HANDLE-" + UUID.randomUUID();
    }

    // 🔹 Step 2: Sync Transactions
    @Transactional
    public void syncTransactions(Long userId, Long bankAccountId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BankAccount bankAccount = bankAccountRepo.findById(bankAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Bank account not found"));

        List<BankTransaction> txns = List.of(
                createTxn(user, bankAccount, 50000, "MONTHLY SALARY CREDIT", BankTransaction.TxnType.CREDIT),
                createTxn(user, bankAccount, 1200, "ZOMATO", BankTransaction.TxnType.DEBIT),
                createTxn(user, bankAccount, 3000, "AMAZON PURCHASE", BankTransaction.TxnType.DEBIT),
                createTxn(user, bankAccount, 800, "RENT PAYMENT", BankTransaction.TxnType.DEBIT)
        );

        for (BankTransaction txn : txns) {
            txnRepo.save(txn);
            mapToIncomeExpense(txn, user, bankAccount);
        }
    }

    // 🔹 Create Bank Transaction
    private BankTransaction createTxn(
            User user,
            BankAccount bankAccount,
            int amt,
            String desc,
            BankTransaction.TxnType type
    ) {
        BankTransaction txn = new BankTransaction();
        txn.setUser(user);
        txn.setBankAccount(bankAccount);
        txn.setAmount(BigDecimal.valueOf(amt));
        txn.setDescription(desc);
        txn.setTxnDate(LocalDate.now());
        txn.setType(type);
        txn.setReferenceId("TXN-" + UUID.randomUUID());
        txn.setCurrency("INR");
        return txn;
    }

    // 🔹 Map Bank Txn → Income / Expense (AA ONLY)
    private void mapToIncomeExpense(
            BankTransaction txn,
            User user,
            BankAccount bankAccount
    ) {
        String desc = txn.getDescription().toLowerCase();

        if (txn.getType() == BankTransaction.TxnType.CREDIT) {

            IncomeRequest req = new IncomeRequest();
            req.setAmount(txn.getAmount());
            req.setDescription(txn.getDescription());
            req.setDate(txn.getTxnDate());

            Income.IncomeCategory category =
                    desc.contains("salary")
                            ? Income.IncomeCategory.SALARY
                            : Income.IncomeCategory.OTHER;

            req.setCategory(category);

            // ✅ CORRECT METHOD
            incomeService.createIncomeFromBank(req, user.getId(), bankAccount);

        } else {

            ExpenseRequest req = new ExpenseRequest();
            req.setAmount(txn.getAmount());
            req.setDescription(txn.getDescription());
            req.setDate(txn.getTxnDate());

            Expense.ExpenseCategory category;
            if (desc.contains("zomato") || desc.contains("swiggy")) {
                category = Expense.ExpenseCategory.FOOD;
            } else if (desc.contains("amazon") || desc.contains("purchase")) {
                category = Expense.ExpenseCategory.SHOPPING;
            } else if (desc.contains("rent")) {
                category = Expense.ExpenseCategory.HOUSING;
            } else {
                category = Expense.ExpenseCategory.OTHER;
            }

            req.setCategory(category);

            // ✅ CORRECT METHOD
            expenseService.createExpenseFromBank(req, user.getId(), bankAccount);
        }
    }
}
