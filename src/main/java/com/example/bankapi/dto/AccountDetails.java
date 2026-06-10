package com.example.bankapi.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Détails complets d'un compte bancaire")
public class AccountDetails {

    @Schema(description = "Identifiant unique du compte", example = "1")
    private Long accountId;

    @Schema(description = "Nom du propriétaire du compte", example = "JAPHET DJOMO")
    private String name;

    @Schema(description = "Email unique du propriétaire", example = "JAPHETDJOM@GMAIL.COM")
    private String email;

    @Schema(description = "Numéro de téléphone", example = "+237657786440")
    private String phone;

    @Schema(description = "Solde actuel du compte", example = "1000.00")
    private BigDecimal balance;

    @Schema(description = "Date et heure de création du compte (ISO 8601)")
    private Instant createdAt;

    @Schema(description = "Date et heure de la dernière modification (ISO 8601)")
    private Instant updatedAt;

    public AccountDetails(Long accountId, String name, String email, String phone, BigDecimal balance, Instant createdAt, Instant updatedAt) {
        this.accountId = accountId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.balance = balance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
