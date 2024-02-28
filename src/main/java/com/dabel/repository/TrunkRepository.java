package com.dabel.repository;

import com.dabel.model.Account;
import com.dabel.model.Trunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrunkRepository extends JpaRepository<Trunk, Long> {
    Optional<Trunk> findByAccount(Account account);
}
