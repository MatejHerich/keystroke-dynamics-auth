package com.example.backend.Repository;

import com.example.backend.Entity.Account;
import com.example.backend.Entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findByAccountOrderByCreatedAtDesc(Account account);
}
