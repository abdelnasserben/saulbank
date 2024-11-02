package com.dabel.service.card;

import com.dabel.app.Helper;
import com.dabel.constant.Status;
import com.dabel.dto.CardDto;
import com.dabel.dto.CardRequestDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.TrunkDto;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.account.AccountService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class CardFacadeService {

    public static final String CARD_IS_ALREADY_ACTIVE_MESSAGE = "Card is already active.";
    public static final String CARD_ACTIVATED_MESSAGE = "Card activated";
    public static final String CANNOT_DEACTIVATE_AN_INACTIVE_CARD_MESSAGE = "Cannot deactivate an inactive card.";

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

    public CardDto getCardById(Long cardId) {
        return cardService.findById(cardId);
    }

    public List<CardDto> getAllCards() {
        return cardService.findAll();
    }

    public List<CardDto> getAllCardsByCustomer(CustomerDto customerDto) {
        List<TrunkDto> trunks = accountService.findAllTrunksByCustomer(customerDto);

        return trunks.stream()
                .map(cardService::findAllByTrunk)
                .flatMap(Collection::stream)
                .toList();
    }

    public void activateCard(Long cardId) {

        CardDto card = cardService.findById(cardId);

        if(Helper.isActiveStatedObject(card))
            throw new IllegalOperationException(CARD_IS_ALREADY_ACTIVE_MESSAGE);

        updateCardStatus(card, Status.ACTIVE.code(), CARD_ACTIVATED_MESSAGE);
    }

    public void deactivateCard(Long cardId, String remarks) {

        CardDto card = cardService.findById(cardId);

        if(!Helper.isActiveStatedObject(card))
            throw new IllegalOperationException(CANNOT_DEACTIVATE_AN_INACTIVE_CARD_MESSAGE);

        updateCardStatus(card, Status.INACTIVE.code(), remarks);
    }

    public void initCardRequest(CardRequestDto cardRequestDto) {
        requestOperationService.init(cardRequestDto);
    }

    public void approveCardRequest(Long requestId) {
        requestOperationService.approve(requestOperationService.getCardRequestService().findById(requestId));
    }

    public void rejectCardRequest(Long requestId, String remarks) {
        requestOperationService.reject(requestOperationService.getCardRequestService().findById(requestId), remarks);
    }

    public List<CardRequestDto> getAllCardRequests() {
        return requestOperationService.getCardRequestService().findAll();
    }

    public CardRequestDto getCardRequestById(Long requestId) {
        return requestOperationService.getCardRequestService().findById(requestId);
    }

    private void updateCardStatus(CardDto card, String status, String remarks) {
        card.setStatus(status);
        card.setFailureReason(remarks);
        card.setUpdatedBy(Helper.getAuthenticated().getName());

        cardService.save(card);
    }
}
