package com.dabel.service.card;

import com.dabel.app.Checker;
import com.dabel.app.Fee;
import com.dabel.constant.Bank;
import com.dabel.constant.LedgerType;
import com.dabel.constant.Status;
import com.dabel.dto.AccountDto;
import com.dabel.dto.CardRequestDto;
import com.dabel.exception.BalanceInsufficientException;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.EvaluableOperation;
import com.dabel.service.fee.FeeService;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
public class CardRequestOperationService implements EvaluableOperation<CardRequestDto> {

    @Getter
    private final CardRequestService cardRequestService;
    private final FeeService feeService;

    public CardRequestOperationService(CardRequestService cardRequestService, FeeService feeService) {
        this.cardRequestService = cardRequestService;
        this.feeService = feeService;
    }

    @Override
    public void init(CardRequestDto cardRequestDto) {

        AccountDto accountDto = cardRequestDto.getTrunk().getAccount();

        if(Checker.isInactiveAccount(accountDto) || Checker.isAssociativeAccount(accountDto))
            throw new IllegalOperationException("The account is not eligible for this operation");

        if(accountDto.getBalance() < Bank.Fees.Card.APPLICATION_REQUEST) {
            cardRequestDto.setStatus(Status.FAILED.code());
            cardRequestDto.setFailureReason("Account balance is insufficient for card request fees");
            cardRequestService.save(cardRequestDto);
            throw new BalanceInsufficientException("Account balance is insufficient for application fees");
        }

        cardRequestDto.setStatus(Status.PENDING.code());
        cardRequestService.save(cardRequestDto);
    }

    @Override
    public void approve(CardRequestDto cardRequestDto) {
        if(!cardRequestDto.getStatus().equals(Status.PENDING.code()))
            return;

        cardRequestDto.setStatus(Status.APPROVED.code());
        //we'll make update by info later...

        //TODO: apply fees
        Fee fee = new Fee(cardRequestDto.getBranch(), Bank.Fees.Card.APPLICATION_REQUEST, "Card application request");
        feeService.apply(cardRequestDto.getTrunk().getAccount(), LedgerType.CARD_REQUEST, fee);

        cardRequestService.save(cardRequestDto);
    }

    @Override
    public void reject(CardRequestDto cardRequestDto, String remarks) {
        cardRequestDto.setStatus(Status.REJECTED.code());
        cardRequestDto.setFailureReason(remarks);
        //we'll make update by info later...

        cardRequestService.save(cardRequestDto);
    }
}
