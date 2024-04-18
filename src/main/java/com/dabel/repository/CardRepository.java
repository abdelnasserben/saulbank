package com.dabel.repository;

import com.dabel.model.Card;
import com.dabel.model.Trunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findAllByTrunk(Trunk trunk);
}
