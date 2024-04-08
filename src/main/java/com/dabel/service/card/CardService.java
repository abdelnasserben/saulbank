package com.dabel.service.card;

import com.dabel.dto.AccountDto;
import com.dabel.dto.CardDto;
import com.dabel.dto.CustomerDto;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.mapper.AccountMapper;
import com.dabel.mapper.CardMapper;
import com.dabel.mapper.CustomerMapper;
import com.dabel.repository.CardRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public CardDto save(CardDto cardDTO) {
        return CardMapper.toDTO(cardRepository.save(CardMapper.toEntity(cardDTO)));
    }

    public CardDto findById(Long cardId) {
        return CardMapper.toDTO(cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found")));
    }

    public CardDto findByCardNumber(String cardNumber) {
        return CardMapper.toDTO(cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found")));
    }

    public List<CardDto> findAllByAccount(AccountDto accountDto) {

        return cardRepository.findAllByAccount(AccountMapper.toModel(accountDto)).stream()
                .map(CardMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<CardDto> findAllByCustomer(CustomerDto customerDto) {
        return cardRepository.findAllByCustomer(CustomerMapper.toModel(customerDto)).stream()
                .map(CardMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<CardDto> findAll() {
        return cardRepository.findAll().stream()
                .map(CardMapper::toDTO)
                .toList();
    }
}
