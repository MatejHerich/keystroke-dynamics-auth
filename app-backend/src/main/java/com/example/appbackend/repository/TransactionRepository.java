package com.example.appbackend.repository;

import com.example.appbackend.model.Account;
import com.example.appbackend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountOrderByTransactionDateDesc(Account account);
}
