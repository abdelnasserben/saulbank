package com.dabel.repository;

import com.dabel.model.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExchangeRepository extends JpaRepository<Exchange, Long> {
    List<Exchange> findAllByCustomerIdentityNumber(String customerIdentity);
}
