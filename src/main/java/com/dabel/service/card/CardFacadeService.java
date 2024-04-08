package com.dabel.service.card;

import com.dabel.app.Helper;
import com.dabel.constant.Status;
import com.dabel.dto.AccountDto;
import com.dabel.dto.CardDto;
import com.dabel.dto.CardRequestDto;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.account.AccountFacadeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardFacadeService {

    private final CardService cardService;
    private final CardRequestService cardRequestService;
    private final CardRequestOperationService requestOperationService;
    private final AccountFacadeService accountFacadeService;

    public CardFacadeService(CardService cardService, CardRequestService cardRequestService, CardRequestOperationService requestOperationService, AccountFacadeService accountFacadeService) {
        this.cardService = cardService;
        this.cardRequestService = cardRequestService;
        this.requestOperationService = requestOperationService;
        this.accountFacadeService = accountFacadeService;
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
        AccountDto accountDto = accountFacadeService.findByNumber(accountNumber);
        return cardService.findAllByAccount(accountDto);
    }

    public List<CardDto> findAllCards() {
        return cardService.findAll();
    }

    public void activateCard(Long cardId) {

        CardDto card = cardService.findById(cardId);

        if(Helper.isActiveCard(card))
            throw new IllegalOperationException("Card already active");

        card.setStatus(Status.ACTIVE.code());
        card.setFailureReason("Activated");
        //we'll set update info later...

        cardService.save(card);
    }

    public void deactivateCard(Long cardId, String remarks) {

        CardDto card = cardService.findById(cardId);

        if(!Helper.isActiveCard(card))
            throw new IllegalOperationException("Unable to deactivate an inactive card");

        card.setStatus(Status.DEACTIVATED.code());
        card.setFailureReason(remarks);
        //we'll set update info later...

        cardService.save(card);
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

    public CardRequestDto findRequestById(Long requestId) {
        return cardRequestService.findById(requestId);
    }

    public List<CardDto> findAllAccountCards(AccountDto accountDto) {
        return cardService.findAllByAccount(accountDto);
    }
}
