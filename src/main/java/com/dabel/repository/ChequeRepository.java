package com.dabel.repository;

import com.dabel.model.Cheque;
import com.dabel.model.Trunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChequeRepository extends JpaRepository<Cheque, Long> {

    Optional<Cheque> findByChequeNumber(String chequeNumber);

    List<Cheque> findAllByTrunk(Trunk trunk);
}
