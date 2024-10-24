package com.dabel.service.card;

import com.dabel.app.Fee;
import com.dabel.app.Helper;
import com.dabel.constant.BankFees;
import com.dabel.constant.LedgerType;
import com.dabel.constant.Status;
import com.dabel.dto.AccountDto;
import com.dabel.dto.CardRequestDto;
import com.dabel.exception.BalanceInsufficientException;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.EvaluableOperation;
import com.dabel.service.fee.FeeService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CardRequestOperationService implements EvaluableOperation<CardRequestDto> {

    @Getter
    private final CardRequestService cardRequestService;
    private final FeeService feeService;

    @Autowired
    public CardRequestOperationService(CardRequestService cardRequestService, FeeService feeService) {
        this.cardRequestService = cardRequestService;
        this.feeService = feeService;
    }

    @Override
    public void init(CardRequestDto cardRequestDto) {

        AccountDto accountDto = cardRequestDto.getTrunk().getAccount();

        if(!Helper.isActiveStatedObject(accountDto) || Helper.isAssociativeAccount(accountDto) || !Helper.isActiveStatedObject(cardRequestDto.getTrunk().getCustomer()))
            throw new IllegalOperationException("The account is not eligible for this operation");

        cardRequestDto.setInitiatedBy(Helper.getAuthenticated().getName());

        if(accountDto.getBalance() < BankFees.Basic.CARD_REQUEST) {
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
        cardRequestDto.setUpdatedBy(Helper.getAuthenticated().getName());
        cardRequestDto.setFailureReason("Approved");

        //TODO: apply fees
        Fee fee = new Fee(cardRequestDto.getBranch(), BankFees.Basic.CARD_REQUEST, "Card application request");
        feeService.apply(cardRequestDto.getTrunk().getAccount(), LedgerType.CARD_REQUEST, fee);

        cardRequestService.save(cardRequestDto);
    }

    @Override
    public void reject(CardRequestDto cardRequestDto, String remarks) {
        cardRequestDto.setStatus(Status.REJECTED.code());
        cardRequestDto.setFailureReason(remarks);
        cardRequestDto.setUpdatedBy(Helper.getAuthenticated().getName());

        cardRequestService.save(cardRequestDto);
    }
}
