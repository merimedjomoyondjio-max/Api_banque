package com.example.bankapi.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Résumé d'un compte bancaire")
public class AccountSummary {

    @Schema(description = "Identifiant unique du compte", example = "1")
    private Long accountId;

    @Schema(description = "Nom du propriétaire", example = "JAPHET DJOMO")
    private String name;

    @Schema(description = "Email du propriétaire", example = "JAPHETDJOM@GMAIL.COM")
    private String email;

    @Schema(description = "Solde actuel du compte", example = "1000.00")
    private BigDecimal balance;

    @Schema(description = "Date de création du compte (ISO 8601)")
    private Instant createdAt;

    public AccountSummary(Long accountId, String name, String email, BigDecimal balance, Instant createdAt) {
        this.accountId = accountId;
        this.name = name;
        this.email = email;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    public Long getAccountId() {
        return accountId;
    }

    @JsonProperty("id")
    public Long getId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
