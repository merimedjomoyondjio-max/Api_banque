package com.example.bankapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String root() {
        return "redirect:/web/login";
    }

    @GetMapping("/login")
    public String legacyLogin() {
        return "redirect:/web/login";
    }
}
