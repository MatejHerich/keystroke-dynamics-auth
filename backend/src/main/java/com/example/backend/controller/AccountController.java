package com.example.backend.controller;

import com.example.backend.entity.Account;
import com.example.backend.service.AccountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/")
    public String home(){
        return "redirect:/account";
    }

    @GetMapping("/account")
    public String showAccount(Principal principal, Model model){
        Account account = accountService.getAccount(principal.getName());
        model.addAttribute("username", principal.getName());
        model.addAttribute("account",account);
        model.addAttribute("transactions", accountService.getTransactions(account));
        return "account";
    }
}
