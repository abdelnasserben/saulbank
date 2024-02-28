package com.dabel.repository;

import com.dabel.dto.AccountDto;
import com.dabel.model.Account;
import com.dabel.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByCardNumber(String cardNumber);

    Collection<Card> findAllByAccount(Account account);
}
