package com.example.bankapi.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.bankapi.dto.AccountDetails;
import com.example.bankapi.dto.AccountSummary;
import com.example.bankapi.dto.CreateAccountRequest;
import com.example.bankapi.dto.PagedResponse;
import com.example.bankapi.dto.TransactionDto;
import com.example.bankapi.service.AccountNotFoundException;
import com.example.bankapi.service.AccountService;
import com.example.bankapi.service.EmailAlreadyExistsException;
import com.example.bankapi.service.InsufficientFundsException;
import com.example.bankapi.service.InvalidAmountException;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/web")
public class WebController {

    private static final int DEFAULT_LIMIT = 8;
    private static final int DEFAULT_TRANSACTION_LIMIT = 8;

    private final AccountService accountService;

    public WebController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public String home() {
        return "redirect:/web/accounts";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/accounts")
    public String accounts(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "8") int limit,
        Model model) {
        populateDashboard(model, page, limit, null);
        return "accounts";
    }

    @GetMapping("/accounts/{accountId}")
    public String accountDetails(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "8") int limit,
        @RequestParam(name = "message", required = false) String message,
        @RequestParam(name = "error", required = false) String error,
        @org.springframework.web.bind.annotation.PathVariable Long accountId,
        Model model) {
        populateDashboard(model, page, limit, accountId);
        if (message != null && !message.isBlank()) {
            model.addAttribute("successMessage", message);
        }
        if (error != null && !error.isBlank()) {
            model.addAttribute("errorMessage", error);
        }
        return "accounts";
    }

    @PostMapping("/accounts/create")
    public String createAccount(
        @Valid @ModelAttribute("createRequest") CreateAccountRequest request,
        BindingResult bindingResult,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "8") int limit,
        Model model,
        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateDashboard(model, page, limit, null);
            return "accounts";
        }

        try {
            AccountDetails created = accountService.createAccount(request);
            redirectAttributes.addFlashAttribute("successMessage", "Compte cree avec succes.");
            return "redirect:/web/accounts/" + created.getAccountId() + "?page=" + page + "&limit=" + limit;
        } catch (EmailAlreadyExistsException | InvalidAmountException ex) {
            populateDashboard(model, page, limit, null);
            model.addAttribute("errorMessage", ex.getMessage());
            return "accounts";
        }
    }

    @PostMapping("/accounts/{accountId}/deposit")
    public String deposit(
        @org.springframework.web.bind.annotation.PathVariable Long accountId,
        @RequestParam BigDecimal amount,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "8") int limit,
        RedirectAttributes redirectAttributes) {
        try {
            accountService.deposit(accountId, amount);
            redirectAttributes.addFlashAttribute("successMessage", "Depot enregistre.");
        } catch (AccountNotFoundException | InvalidAmountException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/web/accounts/" + accountId + "?page=" + page + "&limit=" + limit;
    }

    @PostMapping("/accounts/{accountId}/withdraw")
    public String withdraw(
        @org.springframework.web.bind.annotation.PathVariable Long accountId,
        @RequestParam BigDecimal amount,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "8") int limit,
        RedirectAttributes redirectAttributes) {
        try {
            accountService.withdraw(accountId, amount);
            redirectAttributes.addFlashAttribute("successMessage", "Retrait enregistre.");
        } catch (AccountNotFoundException | InvalidAmountException | InsufficientFundsException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/web/accounts/" + accountId + "?page=" + page + "&limit=" + limit;
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public String handleAccountNotFound(AccountNotFoundException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/web/accounts";
    }

    @ModelAttribute("createRequest")
    public CreateAccountRequest createRequest() {
        return new CreateAccountRequest();
    }

    private void populateDashboard(Model model, int page, int limit, Long selectedAccountId) {
        int sanitizedPage = Math.max(page, 1);
        int sanitizedLimit = Math.max(limit, 1);

        List<AccountSummary> allAccounts = accountService.listAllAccounts();
        PagedResponse<AccountSummary> pagedAccounts = accountService.listAccounts(sanitizedPage, sanitizedLimit);

        BigDecimal totalBalance = allAccounts.stream()
            .map(AccountSummary::getBalance)
            .filter(balance -> balance != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageBalance = allAccounts.isEmpty()
            ? BigDecimal.ZERO
            : totalBalance.divide(BigDecimal.valueOf(allAccounts.size()), 2, RoundingMode.HALF_UP);

        model.addAttribute("accounts", pagedAccounts.getData());
        model.addAttribute("currentPage", pagedAccounts.getCurrentPage());
        model.addAttribute("itemsPerPage", pagedAccounts.getItemsPerPage());
        model.addAttribute("totalItems", pagedAccounts.getTotalItems());
        model.addAttribute("totalPages", pagedAccounts.getTotalPages());
        model.addAttribute("isLast", pagedAccounts.isLast());
        model.addAttribute("accountCount", allAccounts.size());
        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("averageBalance", averageBalance);

        if (selectedAccountId != null) {
            AccountDetails selectedAccount = accountService.getAccountDetails(selectedAccountId);
            List<TransactionDto> transactions = accountService.getAccountTransactions(selectedAccountId, DEFAULT_TRANSACTION_LIMIT);
            model.addAttribute("selectedAccount", selectedAccount);
            model.addAttribute("transactions", transactions);
        }
    }
}
