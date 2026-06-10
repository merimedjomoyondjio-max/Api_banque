package com.example.bankapi.controller;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankapi.dto.AmountRequest;
import com.example.bankapi.dto.CreateAccountRequest;
import com.example.bankapi.model.Account;
import com.example.bankapi.repository.AccountRepository;
import com.example.bankapi.repository.TransactionRepository;
import com.example.bankapi.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests Unitaires pour l'API Bancaire
 * Couvre les 5+1 fonctionnalités principales avec 30 cas de test
 *
 * F1: Créer un compte (7 cas)
 * F2: Lister les comptes (5 cas)
 * F3: Récupérer détails (3 cas)
 * F4: Dépôt (6 cas)
 * F5: Retrait (5 cas)
 * F6: Historique (4 cas)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "testuser", roles = {"USER"})
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountService accountService;

    private Account testAccount1;
    private Account testAccount2;

    @BeforeEach
    void setUp() {
        // Nettoyer les données
        transactionRepository.deleteAll();
        accountRepository.deleteAll();

        // Créer des comptes de test
        testAccount1 = new Account("JAPHET DJOMO", "JAPHETDJOM@GMAIL.COM", "+237657786440", new BigDecimal("1000.00"));
        testAccount2 = new Account("Marie Martin", "marie@example.com", "+33687654321", new BigDecimal("500.00"));

        testAccount1 = accountRepository.save(testAccount1);
        testAccount2 = accountRepository.save(testAccount2);
    }

    // =====================================================================
    // F1: TESTS DE CRÉATION DE COMPTE (7 cas)
    // =====================================================================

    @Test
    @DisplayName("TC1.1: Création valide avec tous les champs")
    void testCreateAccountWithAllFields() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setName("Pierre Durand");
        request.setEmail("pierre@example.com");
        request.setPhone("+33611111111");
        request.setInitialBalance(new BigDecimal("1500.00"));

        mockMvc.perform(post("/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Pierre Durand"))
            .andExpect(jsonPath("$.email").value("pierre@example.com"))
            .andExpect(jsonPath("$.balance").value(1500.00))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("TC1.2: Création valide sans solde initial")
    void testCreateAccountWithoutInitialBalance() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setName("Sophie Bernard");
        request.setEmail("sophie@example.com");
        request.setPhone("+33622222222");
        request.setInitialBalance(null);

        mockMvc.perform(post("/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.balance").value(0.00))
            .andExpect(jsonPath("$.name").value("Sophie Bernard"));
    }

    @Test
    @DisplayName("TC1.3: Email déjà existant")
    void testCreateAccountWithDuplicateEmail() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setName("Autre Personne");
        request.setEmail("JAPHETDJOM@GMAIL.COM");
        request.setPhone("+33611111111");
        request.setInitialBalance(new BigDecimal("100.00"));

        mockMvc.perform(post("/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("TC1.4: Email invalide")
    void testCreateAccountWithInvalidEmail() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setName("Paul");
        request.setEmail("email-invalide");
        request.setPhone("+33611111111");

        mockMvc.perform(post("/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC1.5: Téléphone invalide (moins de 10 chiffres)")
    void testCreateAccountWithInvalidPhone() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setName("Paul");
        request.setEmail("paul@example.com");
        request.setPhone("12345");

        mockMvc.perform(post("/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC1.6: Solde initial négatif")
    void testCreateAccountWithNegativeBalance() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setName("Luc");
        request.setEmail("luc@example.com");
        request.setPhone("+33633333333");
        request.setInitialBalance(new BigDecimal("-100.00"));

        mockMvc.perform(post("/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC1.7: Champ requis manquant (name)")
    void testCreateAccountWithMissingName() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setName(null);
        request.setEmail("test@example.com");
        request.setPhone("+237657786440");

        mockMvc.perform(post("/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    // =====================================================================
    // F2: TESTS DE LISTAGE DES COMPTES (5 cas)
    // =====================================================================

    @Test
    @DisplayName("TC2.1: Première page avec pagination par défaut")
    void testListAccountsFirstPage() throws Exception {
        // Créer 25 comptes supplémentaires
        for (int i = 3; i <= 25; i++) {
            Account account = new Account(
                "User " + i,
                "user" + i + "@example.com",
                "+3361" + String.format("%07d", i),
                new BigDecimal("100.00")
            );
            accountRepository.save(account);
        }

        mockMvc.perform(get("/accounts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(25))
            .andExpect(jsonPath("$.totalPages").value(3)) // 25/10 = 3 pages
            .andExpect(jsonPath("$.currentPage").value(1))
            .andExpect(jsonPath("$.results.length()").value(10))
            .andExpect(jsonPath("$.isLast").value(false));
    }

    @Test
    @DisplayName("TC2.2: Dernière page")
    void testListAccountsLastPage() throws Exception {
        // Créer 25 comptes
        for (int i = 3; i <= 25; i++) {
            Account account = new Account(
                "User " + i,
                "user" + i + "@example.com",
                "+3361" + String.format("%07d", i),
                new BigDecimal("100.00")
            );
            accountRepository.save(account);
        }

        mockMvc.perform(get("/accounts?page=3&limit=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.results.length()").value(5)) // 25 % 10 = 5
            .andExpect(jsonPath("$.isLast").value(true))
            .andExpect(jsonPath("$.currentPage").value(3));
    }

    @Test
    @DisplayName("TC2.3: Limite personnalisée")
    void testListAccountsWithCustomLimit() throws Exception {
        mockMvc.perform(get("/accounts?page=1&limit=1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.results.length()").value(1))
            .andExpect(jsonPath("$.totalElements").value(2)); // 2 comptes de test
    }

    @Test
    @DisplayName("TC2.4: Aucun compte (BDD vide)")
    void testListAccountsEmpty() throws Exception {
        accountRepository.deleteAll();

        mockMvc.perform(get("/accounts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.results.length()").value(0))
            .andExpect(jsonPath("$.totalElements").value(0))
            .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    @DisplayName("TC2.5: Pagination avancée")
    void testListAccountsPagination() throws Exception {
        mockMvc.perform(get("/accounts?page=1&limit=2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.results.length()").value(2))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.isLast").value(true));
    }

    // =====================================================================
    // F3: TESTS DE RÉCUPÉRATION DES DÉTAILS (3 cas)
    // =====================================================================

    @Test
    @DisplayName("TC3.1: Compte existant")
    void testGetAccountDetails() throws Exception {
        mockMvc.perform(get("/accounts/" + testAccount1.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(testAccount1.getId()))
            .andExpect(jsonPath("$.name").value("JAPHET DJOMO"))
            .andExpect(jsonPath("$.email").value("JAPHETDJOM@GMAIL.COM"))
            .andExpect(jsonPath("$.phone").value("+237657786440"))
            .andExpect(jsonPath("$.balance").value(1000.00))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("TC3.2: Compte inexistant")
    void testGetAccountDetailsNotFound() throws Exception {
        mockMvc.perform(get("/accounts/99999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("TC3.3: ID format invalide")
    void testGetAccountDetailsInvalidId() throws Exception {
        mockMvc.perform(get("/accounts/abc"))
            .andExpect(status().isBadRequest());
    }

    // =====================================================================
    // F4: TESTS DE DÉPÔT (6 cas)
    // =====================================================================

    @Test
    @DisplayName("TC4.1: Dépôt valide")
    void testDepositValid() throws Exception {
        AmountRequest request = new AmountRequest();
        request.setAmount(new BigDecimal("50.00"));

        mockMvc.perform(post("/accounts/" + testAccount1.getId() + "/deposit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.newBalance").value(1050.00))
            .andExpect(jsonPath("$.accountId").value(testAccount1.getId()));

        // Vérifier que la transaction a été créée
        Account updatedAccount = accountRepository.findById(testAccount1.getId()).orElseThrow();
        assert updatedAccount.getBalance().compareTo(new BigDecimal("1050.00")) == 0;
    }

    @Test
    @DisplayName("TC4.2: Dépôt sur compte inexistant")
    void testDepositAccountNotFound() throws Exception {
        AmountRequest request = new AmountRequest();
        request.setAmount(new BigDecimal("50.00"));

        mockMvc.perform(post("/accounts/99999/deposit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("TC4.3: Montant invalide (zéro)")
    void testDepositZeroAmount() throws Exception {
        AmountRequest request = new AmountRequest();
        request.setAmount(new BigDecimal("0.00"));

        mockMvc.perform(post("/accounts/" + testAccount1.getId() + "/deposit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC4.4: Montant négatif")
    void testDepositNegativeAmount() throws Exception {
        AmountRequest request = new AmountRequest();
        request.setAmount(new BigDecimal("-100.00"));

        mockMvc.perform(post("/accounts/" + testAccount1.getId() + "/deposit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC4.5: Montant très petit")
    void testDepositSmallAmount() throws Exception {
        AmountRequest request = new AmountRequest();
        request.setAmount(new BigDecimal("0.01"));

        mockMvc.perform(post("/accounts/" + testAccount1.getId() + "/deposit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.newBalance").value(1000.01));
    }

    @Test
    @DisplayName("TC4.6: Montant très grand")
    void testDepositLargeAmount() throws Exception {
        AmountRequest request = new AmountRequest();
        request.setAmount(new BigDecimal("999999999.99"));

        mockMvc.perform(post("/accounts/" + testAccount1.getId() + "/deposit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.newBalance").value(1000000999.99));
    }

    // =====================================================================
    // F5: TESTS DE RETRAIT (5 cas)
    // =====================================================================

    @Test
    @DisplayName("TC5.1: Retrait valide")
    void testWithdrawValid() throws Exception {
        AmountRequest request = new AmountRequest();
        request.setAmount(new BigDecimal("150.00"));

        mockMvc.perform(post("/accounts/" + testAccount1.getId() + "/withdraw")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.newBalance").value(850.00))
            .andExpect(jsonPath("$.accountId").value(testAccount1.getId()));
    }

    @Test
    @DisplayName("TC5.2: Fonds insuffisants")
    void testWithdrawInsufficientFunds() throws Exception {
        AmountRequest request = new AmountRequest();
        request.setAmount(new BigDecimal("1000.00")); // Compte a 500

        mockMvc.perform(post("/accounts/" + testAccount2.getId() + "/withdraw")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnprocessableEntity());

        // Vérifier que le solde n'a pas changé
        Account unchanged = accountRepository.findById(testAccount2.getId()).orElseThrow();
        assert unchanged.getBalance().compareTo(new BigDecimal("500.00")) == 0;
    }

    @Test
    @DisplayName("TC5.3: Retrait du solde exact")
    void testWithdrawExactBalance() throws Exception {
        AmountRequest request = new AmountRequest();
        request.setAmount(new BigDecimal("500.00"));

        mockMvc.perform(post("/accounts/" + testAccount2.getId() + "/withdraw")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.newBalance").value(0.00));
    }

    @Test
    @DisplayName("TC5.4: Retrait sur compte inexistant")
    void testWithdrawAccountNotFound() throws Exception {
        AmountRequest request = new AmountRequest();
        request.setAmount(new BigDecimal("50.00"));

        mockMvc.perform(post("/accounts/99999/withdraw")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("TC5.5: Montant invalide")
    void testWithdrawInvalidAmount() throws Exception {
        AmountRequest request = new AmountRequest();
        request.setAmount(new BigDecimal("-50.00"));

        mockMvc.perform(post("/accounts/" + testAccount1.getId() + "/withdraw")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    // =====================================================================
    // F6: TESTS DE TRANSACTIONS (4 cas)
    // =====================================================================

    @Test
    @DisplayName("TC6.1: Historique avec transactions")
    void testGetTransactionsWithHistory() throws Exception {
        // Créer des transactions
        accountService.deposit(testAccount1.getId(), new BigDecimal("100.00"));
        accountService.withdraw(testAccount1.getId(), new BigDecimal("50.00"));
        accountService.deposit(testAccount1.getId(), new BigDecimal("200.00"));

        mockMvc.perform(get("/accounts/" + testAccount1.getId() + "/transactions?limit=20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].type").exists())
            .andExpect(jsonPath("$[0].amount").exists())
            .andExpect(jsonPath("$[0].timestamp").exists());
    }

    @Test
    @DisplayName("TC6.2: Historique limité")
    void testGetTransactionsLimited() throws Exception {
        // Créer 25 transactions
        for (int i = 0; i < 25; i++) {
            if (i % 2 == 0) {
                accountService.deposit(testAccount1.getId(), new BigDecimal("10.00"));
            } else {
                accountService.withdraw(testAccount1.getId(), new BigDecimal("5.00"));
            }
        }

        mockMvc.perform(get("/accounts/" + testAccount1.getId() + "/transactions?limit=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(10));
    }

    @Test
    @DisplayName("TC6.3: Compte sans transactions")
    void testGetTransactionsEmpty() throws Exception {
        mockMvc.perform(get("/accounts/" + testAccount1.getId() + "/transactions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("TC6.4: Compte inexistant")
    void testGetTransactionsAccountNotFound() throws Exception {
        mockMvc.perform(get("/accounts/99999/transactions"))
            .andExpect(status().isNotFound());
    }
}
