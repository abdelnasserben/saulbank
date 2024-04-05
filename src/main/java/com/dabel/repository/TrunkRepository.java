package com.dabel.repository;

import com.dabel.model.Account;
import com.dabel.model.Customer;
import com.dabel.model.Trunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrunkRepository extends JpaRepository<Trunk, Long> {

    List<Trunk> findAllByCustomer(Customer customer);

    List<Trunk> findAllByAccount(Account account);

    Trunk findByCustomerAndAccount(Customer customer, Account account);

}
