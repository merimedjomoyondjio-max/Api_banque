package com.example.bankapi.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankapi.dto.RegisterRequest;
import com.example.bankapi.model.AppUser;
import com.example.bankapi.repository.AppUserRepository;

import jakarta.validation.Valid;

@Service
public class AppUserService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(@Valid RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas");
        }
        if (appUserRepository.existsByUsername(request.getUsername())) {
            throw new IllegalStateException("Ce nom d'utilisateur est déjà utilisé");
        }
        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Cet email est déjà utilisé");
        }

        AppUser user = new AppUser(
            request.getFullName(),
            request.getUsername(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            "ROLE_USER"
        );
        return appUserRepository.save(user);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) {
        AppUser user = appUserRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));

        return org.springframework.security.core.userdetails.User
            .withUsername(user.getUsername())
            .password(user.getPasswordHash())
            .authorities(new SimpleGrantedAuthority(user.getRole()))
            .build();
    }
}
