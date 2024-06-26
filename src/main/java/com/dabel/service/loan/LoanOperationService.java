package com.dabel.service.loan;

import com.dabel.app.Fee;
import com.dabel.app.Helper;
import com.dabel.constant.*;
import com.dabel.dto.AccountDto;
import com.dabel.dto.LoanDto;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.EvaluableOperation;
import com.dabel.service.account.AccountService;
import com.dabel.service.fee.FeeService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanOperationService implements EvaluableOperation<LoanDto> {

    @Getter
    private final LoanService loanService;
    private final AccountService accountService;
    private final FeeService feeService;

    @Autowired
    public LoanOperationService(LoanService loanService, AccountService accountService, FeeService feeService) {
        this.loanService = loanService;
        this.accountService = accountService;
        this.feeService = feeService;
    }

    @Override
    public void init(LoanDto loanDto) {

        if(!Helper.isActiveStatedObject(loanDto.getBorrower()))
            throw new IllegalOperationException("Borrower must be active");

        double loanAmount = Helper.calculateTotalAmountOfLoan(loanDto.getIssuedAmount(), loanDto.getInterestRate());

        //TODO: create account of loan
        AccountDto savedAccount = accountService.save(AccountDto.builder()
                .accountName(String.format("%s %s", loanDto.getBorrower().getFirstName(), loanDto.getBorrower().getLastName()))
                .accountNumber(Helper.generateAccountNumber())
                .accountType(AccountType.LOAN.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .currency(Currency.KMF.name())
                .balance(-loanAmount)
                .status(Status.PENDING.code())
                .branch(loanDto.getBranch())
                .initiatedBy(Helper.getAuthenticated().getName())
                .build());

        //TODO: update loan info before saving
        loanDto.setAccount(savedAccount);
        loanDto.setTotalAmount(loanAmount);
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

        //TODO: apply withdraw fees
        Fee fee = new Fee(loanDto.getBranch(), loanDto.getApplicationFees(), "Loan");
        feeService.apply(loanDto.getAccount(), LedgerType.LOAN, fee);

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
