package com.example.backend.Controller;

import com.example.backend.Service.RegistrationService;
import com.example.backend.dto.RegistrationForm;
import com.example.backend.exception.UsernameAlreadyExistsException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/register")
public class AuthController {
    private RegistrationService registrationService;

    public AuthController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping
    public String register(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping
    public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm registrationForm, BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors("passwordConfirm") && !registrationForm.passwordsMatch()) {
            bindingResult.rejectValue("passwordConfirm", "mismatch", "Heslá sa nezhodujú.");
        }
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            registrationService.register(registrationForm);
        } catch (UsernameAlreadyExistsException e) {
            bindingResult.rejectValue("username", "taken", e.getMessage());
            return "register";
        }
        return "redirect:/login";
    }
}
