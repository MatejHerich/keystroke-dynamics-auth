package com.example.backend.repository;

import com.example.backend.entity.Account;
import com.example.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findByAccountOrderByCreatedAtDesc(Account account);
}
