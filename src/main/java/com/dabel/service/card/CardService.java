package com.dabel.service.card;

import com.dabel.dto.CardDto;
import com.dabel.dto.TrunkDto;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.mapper.CardMapper;
import com.dabel.mapper.TrunkMapper;
import com.dabel.repository.CardRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public CardDto save(CardDto cardDTO) {
        return CardMapper.toDto(cardRepository.save(CardMapper.toEntity(cardDTO)));
    }

    public CardDto findById(Long cardId) {
        return CardMapper.toDto(cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found")));
    }

    public CardDto findByCardNumber(String cardNumber) {
        return CardMapper.toDto(cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found")));
    }

    public List<CardDto> findAll() {
        return cardRepository.findAll().stream()
                .map(CardMapper::toDto)
                .toList();
    }

    public List<CardDto> findAllByTrunk(TrunkDto trunkDto) {
        return cardRepository.findAllByTrunk(TrunkMapper.toModel(trunkDto)).stream()
                .map(CardMapper::toDto)
                .toList();
    }
}
