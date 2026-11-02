package com.example.backend.repository;

import com.example.backend.entity.Account;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByUser(User user);
    Optional<Account> findByIban(String iban);
}
