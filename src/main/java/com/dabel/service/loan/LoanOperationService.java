package com.dabel.service.loan;

import com.dabel.app.Helper;
import com.dabel.constant.AccountProfile;
import com.dabel.constant.AccountType;
import com.dabel.constant.Currency;
import com.dabel.constant.Status;
import com.dabel.dto.AccountDto;
import com.dabel.dto.LoanDto;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.EvaluableOperation;
import com.dabel.service.account.AccountService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanOperationService implements EvaluableOperation<LoanDto> {

    @Getter
    private final LoanService loanService;
    private final AccountService accountService;

    @Autowired
    public LoanOperationService(LoanService loanService, AccountService accountService) {
        this.loanService = loanService;
        this.accountService = accountService;
    }

    @Override
    public void init(LoanDto loanDto) {

        if(!Helper.isActiveStatedObject(loanDto.getBorrower()))
            throw new IllegalOperationException("Borrower must be active");

        double loanAmount = Helper.calculateTotalAmountOfLoan(loanDto.getIssuedAmount(), loanDto.getInterestRate());
        double totalAmount = loanAmount + loanDto.getApplicationFees();

        //TODO: create account of loan
        AccountDto savedAccount = accountService.save(AccountDto.builder()
                .accountName(String.format("%s %s", loanDto.getBorrower().getFirstName(), loanDto.getBorrower().getLastName()))
                .accountNumber(Helper.generateAccountNumber())
                .accountType(AccountType.LOAN.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .currency(Currency.KMF.name())
                .balance(totalAmount)
                .status(Status.PENDING.code())
                .branch(loanDto.getBranch())
                .initiatedBy(Helper.getAuthenticated().getName())
                .build());

        //TODO: update loan info before saving
        loanDto.setAccount(savedAccount);
        loanDto.setTotalAmount(totalAmount);
        loanDto.setCurrency(Currency.KMF.name());
        loanDto.setStatus(Status.PENDING.code());
        loanDto.setInitiatedBy(Helper.getAuthenticated().getName());

        loanService.save(loanDto);
    }

    @Override
    public void approve(LoanDto loanDto) {

        String currentUsername = Helper.getAuthenticated().getName();

        loanDto.setStatus(Status.ACTIVE.code());
        loanDto.setFailureReason("Approved");
        loanDto.setUpdatedBy(currentUsername);

        //TODO: active loan account
        AccountDto loanAccount = loanDto.getAccount();
        loanAccount.setStatus(Status.ACTIVE.code());
        loanAccount.setUpdatedBy(currentUsername);
        accountService.save(loanAccount);

        loanService.save(loanDto);
    }

    @Override
    public void reject(LoanDto loanDto, String remarks) {

        String currentUsername = Helper.getAuthenticated().getName();

        loanDto.setStatus(Status.REJECTED.code());
        loanDto.setFailureReason(remarks);
        loanDto.setUpdatedBy(currentUsername);

        AccountDto loanAccount = loanDto.getAccount();
        loanAccount.setStatus(Status.REJECTED.code());
        loanAccount.setUpdatedBy(currentUsername);
        accountService.save(loanAccount);

        loanService.save(loanDto);
    }
}
