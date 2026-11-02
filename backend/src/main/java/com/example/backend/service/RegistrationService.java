package com.example.backend.service;

import com.example.backend.entity.Account;
import com.example.backend.entity.User;
import com.example.backend.repository.AccountRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.dto.RegistrationForm;
import com.example.backend.exception.UsernameAlreadyExistsException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class RegistrationService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BigDecimal initialBalance;

    public RegistrationService(AccountRepository accountRepository, UserRepository userRepository, PasswordEncoder passwordEncoder,@Value("${app.initial-balance}") BigDecimal initialBalance) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.initialBalance = initialBalance;
    }

    @Transactional
    public User register(RegistrationForm registrationForm) {
        if(userRepository.existsByUsername(registrationForm.getUsername())){
            throw  new UsernameAlreadyExistsException(registrationForm.getUsername());
        }
        User user = new User();
        user.setUsername(registrationForm.getUsername());
        user.setPasswordHash(passwordEncoder.encode(registrationForm.getPassword()));
        User savedUser = userRepository.save(user);

        Account account = new Account();
        account.setUser(savedUser);
        account.setIban(createIban(savedUser.getId()));
        account.setBalance(initialBalance);
        accountRepository.save(account);

        return savedUser;
    }

    private String createIban(Integer userId) {
        return String.format("SK00%020d", userId);
    }
}
