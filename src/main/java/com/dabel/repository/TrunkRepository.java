package com.dabel.repository;

import com.dabel.model.Account;
import com.dabel.model.Customer;
import com.dabel.model.Trunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrunkRepository extends JpaRepository<Trunk, Long> {
    Optional<Trunk> findByAccount(Account account);

    List<Trunk> findAllByCustomer(Customer customer);

    Trunk findByCustomerAndAccount(Customer customer, Account account);
}
