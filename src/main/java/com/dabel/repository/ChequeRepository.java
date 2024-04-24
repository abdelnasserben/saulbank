package com.dabel.repository;

import com.dabel.model.Cheque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChequeRepository extends JpaRepository<Cheque, Long> {

    Optional<Cheque> findByChequeNumber(String chequeNumber);
}
