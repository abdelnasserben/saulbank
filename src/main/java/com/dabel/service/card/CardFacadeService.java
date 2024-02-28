package com.dabel.service.card;

import com.dabel.dto.AccountDto;
import com.dabel.dto.CardDto;
import com.dabel.dto.CardRequestDto;
import com.dabel.service.account.AccountService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardFacadeService {

    private final CardService cardService;
    private final CardRequestOperationService requestOperationService;
    private final AccountService accountService;

    public CardFacadeService(CardService cardService, CardRequestOperationService requestOperationService, AccountService accountService) {
        this.cardService = cardService;
        this.requestOperationService = requestOperationService;
        this.accountService = accountService;
    }

    public void saveCard(CardDto cardDto) {
        cardService.save(cardDto);
    }

    public CardDto findCardById(Long cardId) {
        return cardService.findById(cardId);
    }

    public CardDto findCardByCardNumber(String cardNumber) {
        return cardService.findByCardNumber(cardNumber);
    }

    public List<CardDto> findAllCardsOfAnAccount(String accountNumber) {
        AccountDto accountDto = accountService.findByNumber(accountNumber);
        return cardService.findAllByAccount(accountDto);
    }

    public List<CardDto> findAllCards() {
        return cardService.findAll();
    }

    /**
     * For card requests
     */

    public void sendRequest(CardRequestDto cardRequestDto) {
        requestOperationService.init(cardRequestDto);
    }

    public void approveRequest(Long requestId) {
        requestOperationService.approve(requestOperationService.getCardRequestService().findById(requestId));
    }

    public void rejectRequest(Long requestId, String remarks) {
        requestOperationService.reject(requestOperationService.getCardRequestService().findById(requestId), remarks);
    }

    public List<CardRequestDto> findAllCardRequests() {
        return requestOperationService.getCardRequestService().findAll();
    }
}
