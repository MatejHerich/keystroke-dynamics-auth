package com.example.backend.service;

import com.example.backend.entity.Account;
import com.example.backend.entity.Transaction;
import com.example.backend.entity.User;
import com.example.backend.repository.AccountRepository;
import com.example.backend.repository.TransactionRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public Account getAccount(String username){
        User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalStateException("User not found: " + username));
        return accountRepository.findByUser(user).orElseThrow(()-> new IllegalStateException("Account not found: " + username));
    }

    @Transactional(readOnly = true)
    public List<Transaction>  getTransactions(Account account){
        return transactionRepository.findByAccountOrderByCreatedAtDesc(account);
    }
}
