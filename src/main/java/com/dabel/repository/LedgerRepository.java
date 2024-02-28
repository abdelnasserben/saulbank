package com.dabel.repository;

import com.dabel.model.Branch;
import com.dabel.model.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LedgerRepository extends JpaRepository<Ledger, Long> {
    Optional<Ledger> findByBranchAndLedgerType(Branch branch, String ledgerType);
}
