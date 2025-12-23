package com.vijay.User_Master.controller.view;

import com.vijay.User_Master.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final HomeService homeService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword() {
        return "reset-password";
    }

    @GetMapping("/verify-account")
    public String verifyAccount(@RequestParam Long uid, @RequestParam String code, Model model) {
        try {
            Boolean isVerified = homeService.verifyAccount(uid, code);
            model.addAttribute("verified", isVerified);
        } catch (Exception e) {
            model.addAttribute("verified", false);
            model.addAttribute("error", e.getMessage());
        }
        return "verify-account";
    }
}
