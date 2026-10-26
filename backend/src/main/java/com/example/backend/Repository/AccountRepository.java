package com.example.backend.Repository;

import com.example.backend.Entity.Account;
import com.example.backend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByUser(User user);
    Optional<Account> findByIban(String iban);
}
