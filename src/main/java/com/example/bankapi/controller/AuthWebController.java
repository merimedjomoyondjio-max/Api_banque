package com.example.bankapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.bankapi.dto.RegisterRequest;
import com.example.bankapi.service.AppUserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/web")
public class AuthWebController {

    private final AppUserService appUserService;

    public AuthWebController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(
        @Valid @ModelAttribute("registerRequest") RegisterRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "passwordMismatch", "Les mots de passe ne correspondent pas");
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            appUserService.register(request);
            redirectAttributes.addFlashAttribute("successMessage", "Compte créé avec succès. Connectez-vous.");
            return "redirect:/web/login";
        } catch (IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("registrationError", ex.getMessage());
            return "register";
        }
    }
}
