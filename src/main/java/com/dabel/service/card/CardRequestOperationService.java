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
import org.springframework.stereotype.Service;

@Service
public class CardRequestOperationService implements EvaluableOperation<CardRequestDto> {

    @Getter
    private final CardRequestService cardRequestService;
    private final FeeService feeService;

    public static final String ACCOUNT_BALANCE_IS_INSUFFICIENT_FOR_CARD_REQUEST_FEES_MESSAGE = "Account balance is insufficient for card request fees";


    public CardRequestOperationService(CardRequestService cardRequestService, FeeService feeService) {
        this.cardRequestService = cardRequestService;
        this.feeService = feeService;
    }

    /**
     * Initializes a card request by verifying account eligibility, status, and balance.
     * Sets the request as pending if all conditions are met.
     *
     * @param cardRequestDto The card request data transfer object.
     * @throws IllegalOperationException If the account or customer is inactive or the account is associative.
     * @throws BalanceInsufficientException If the account balance is insufficient for the request fee.
     */
    @Override
    public void init(CardRequestDto cardRequestDto) {

        AccountDto accountDto = cardRequestDto.getTrunk().getAccount();
        String authenticatedUsername = Helper.getAuthenticated().getName();

        validateAccountEligibility(accountDto, cardRequestDto);

        cardRequestDto.setInitiatedBy(authenticatedUsername);

        if (!hasSufficientBalance(accountDto)) {
            saveFailedRequest(cardRequestDto);
            throw new BalanceInsufficientException(ACCOUNT_BALANCE_IS_INSUFFICIENT_FOR_CARD_REQUEST_FEES_MESSAGE);
        }

        cardRequestDto.setStatus(Status.PENDING.code());
        cardRequestService.save(cardRequestDto);
    }

    @Override
    public void approve(CardRequestDto cardRequestDto) {
        if (!Status.PENDING.code().equals(cardRequestDto.getStatus())) {
            return; // Only pending requests can be approved
        }

        updateCardRequestStatus(cardRequestDto, Status.APPROVED, "Approved");
        applyCardRequestFee(cardRequestDto);
        cardRequestService.save(cardRequestDto);
    }

    @Override
    public void reject(CardRequestDto cardRequestDto, String remarks) {

        updateCardRequestStatus(cardRequestDto, Status.REJECTED, remarks);
        cardRequestService.save(cardRequestDto);
    }

    private void updateCardRequestStatus(CardRequestDto cardRequestDto, Status status, String reason) {
        cardRequestDto.setStatus(status.code());
        cardRequestDto.setFailureReason(reason);
        cardRequestDto.setUpdatedBy(Helper.getAuthenticated().getName());
    }

    private void applyCardRequestFee(CardRequestDto cardRequestDto) {
        Fee fee = new Fee(
                cardRequestDto.getBranch(),
                BankFees.Basic.CARD_REQUEST,
                "Card application request"
        );
        feeService.apply(cardRequestDto.getTrunk().getAccount(), LedgerType.CARD_REQUEST, fee);
    }

    private void validateAccountEligibility(AccountDto accountDto, CardRequestDto requestDto) {
        if (!Helper.isActiveStatedObject(accountDto) || Helper.isAssociativeAccount(accountDto)
                || !Helper.isActiveStatedObject(requestDto.getTrunk().getCustomer())) {
            throw new IllegalOperationException("The account is not eligible for this operation");
        }
    }

    private boolean hasSufficientBalance(AccountDto accountDto) {
        return accountDto.getBalance() >= BankFees.Basic.CARD_REQUEST;
    }

    private void saveFailedRequest(CardRequestDto requestDto) {
        requestDto.setStatus(Status.FAILED.code());
        requestDto.setFailureReason(CardRequestOperationService.ACCOUNT_BALANCE_IS_INSUFFICIENT_FOR_CARD_REQUEST_FEES_MESSAGE);
        cardRequestService.save(requestDto);
    }
}
