package com.example.bankapi.service;

import com.example.bankapi.dto.*;
import com.example.bankapi.model.Account;
import com.example.bankapi.model.Transaction;
import com.example.bankapi.repository.AccountRepository;
import com.example.bankapi.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public AccountDetails createAccount(CreateAccountRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException();
        }
        BigDecimal balance = request.getInitialBalance() == null ? BigDecimal.ZERO : request.getInitialBalance();
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAmountException();
        }
        Account account = new Account(request.getName(), request.getEmail(), request.getPhone(), balance);
        Account savedAccount = accountRepository.save(account);
        return toAccountDetails(savedAccount);
    }

    @Transactional(readOnly = true)
    public List<AccountSummary> listAllAccounts() {
        return accountRepository.findAll().stream()
            .map(this::toSummary)
            .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<AccountSummary> listAccounts(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit); // page starts from 1
        Page<Account> accountPage = accountRepository.findAll(pageable);
        List<AccountSummary> summaries = accountPage.getContent().stream()
            .map(this::toSummary)
            .toList();
        return new PagedResponse<>(summaries, accountPage.getNumber() + 1, accountPage.getSize(), accountPage.getTotalElements(), accountPage.getTotalPages(), accountPage.isLast());
    }

    @Transactional(readOnly = true)
    public AccountDetails getAccountDetails(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
        return toAccountDetails(account);
    }

    @Transactional
    public DepositResponse deposit(Long id, BigDecimal amount) {
        validateAmount(amount);
        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);

        Transaction transaction = new Transaction(account, Transaction.TransactionType.DEPOSIT, amount, newBalance, "Dépôt espèces");
        transactionRepository.save(transaction);

        return new DepositResponse("Dépôt effectué", account.getId(), newBalance);
    }

    @Transactional
    public WithdrawResponse withdraw(Long id, BigDecimal amount) {
        validateAmount(amount);
        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }
        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);

        Transaction transaction = new Transaction(account, Transaction.TransactionType.WITHDRAWAL, amount, newBalance, "Retrait espèces");
        transactionRepository.save(transaction);

        return new WithdrawResponse("Retrait effectué", account.getId(), newBalance);
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getAccountTransactions(Long accountId, int limit) {
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }
        List<Transaction> transactions = transactionRepository.findByAccountIdOrderByTimestampDesc(accountId);
        return transactions.stream()
            .limit(limit)
            .map(this::toTransactionDto)
            .toList();
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException();
        }
    }

    private AccountSummary toSummary(Account account) {
        return new AccountSummary(
            account.getId(),
            account.getOwnerName(),
            account.getEmail(),
            account.getBalance(),
            account.getCreatedAt()
        );
    }

    private AccountDetails toAccountDetails(Account account) {
        return new AccountDetails(
            account.getId(),
            account.getOwnerName(),
            account.getEmail(),
            account.getPhone(),
            account.getBalance(),
            account.getCreatedAt(),
            account.getUpdatedAt()
        );
    }

    private TransactionDto toTransactionDto(Transaction transaction) {
        return new TransactionDto(
            transaction.getTransactionId(),
            transaction.getAccount().getId(),
            transaction.getType(),
            transaction.getAmount(),
            transaction.getNewBalance(),
            transaction.getTimestamp(),
            transaction.getDescription()
        );
    }
}

