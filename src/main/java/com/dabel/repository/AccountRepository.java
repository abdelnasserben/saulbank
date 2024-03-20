package com.dabel.repository;

import com.dabel.model.Account;
import com.dabel.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByBranchAndCurrencyAndIsVault(Branch branch, String currency, int isVault);

    List<Account> findAllByBranchAndIsVault(Branch branch, int isVault);
}
