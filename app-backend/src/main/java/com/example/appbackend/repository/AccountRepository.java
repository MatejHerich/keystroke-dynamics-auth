package com.example.appbackend.repository;

import com.example.appbackend.model.Account;
import com.example.appbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByUser(User user);
    Account findByIban(String iban);
}
