package com.dabel.repository;

import com.dabel.model.Customer;
import com.dabel.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findAllByBorrower(Customer borrower);
}
