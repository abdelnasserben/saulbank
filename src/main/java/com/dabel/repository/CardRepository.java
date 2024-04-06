package com.dabel.repository;

import com.dabel.model.Account;
import com.dabel.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByCardNumber(String cardNumber);

    List<Card> findAllByAccount(Account account);
}
