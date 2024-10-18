package com.dabel.service.loan;

import com.dabel.app.Fee;
import com.dabel.app.Helper;
import com.dabel.constant.*;
import com.dabel.dto.AccountDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.LoanDto;
import com.dabel.dto.LoanRequestDto;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.EvaluableOperation;
import com.dabel.service.account.AccountService;
import com.dabel.service.fee.FeeService;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
public class LoanRequestOperationService implements EvaluableOperation<LoanRequestDto> {

    @Getter
    private final LoanRequestService loanRequestService;
    private final AccountService accountService;
    private final LoanService loanService;
    private final FeeService feeService;

    public LoanRequestOperationService(LoanRequestService loanRequestService, AccountService accountService, LoanService loanService, FeeService feeService) {
        this.loanRequestService = loanRequestService;
        this.accountService = accountService;
        this.loanService = loanService;
        this.feeService = feeService;
    }

    @Override
    public void init(LoanRequestDto loanRequestDto) {

        //TODO: check if borrower and his associated account are active
        if(!Helper.isActiveStatedObject(loanRequestDto.getBorrower()) || !Helper.isActiveStatedObject(loanRequestDto.getAssociatedAccount()))
            throw new IllegalOperationException("Borrower and his associated account must be active");

        //TODO: save request
        loanRequestDto.setStatus(Status.PENDING.code());
        loanRequestDto.setInitiatedBy(Helper.getAuthenticated().getName());
        loanRequestService.save(loanRequestDto);
    }

    @Override
    public void approve(LoanRequestDto loanRequestDto) {

        double loanTotalAmount = Helper.calculateTotalAmountOfLoan(loanRequestDto.getRequestedAmount(), loanRequestDto.getInterestRate());
        CustomerDto borrower = loanRequestDto.getBorrower();

        //TODO: create loan account
        AccountDto savedLoanAccount = accountService.save(
                AccountDto.builder()
                        .accountName(String.format("%s %s", borrower.getFirstName(), borrower.getLastName()))
                        .accountNumber(Helper.generateAccountNumber())
                        .accountType(AccountType.LOAN.name())
                        .accountProfile(AccountProfile.PERSONAL.name())
                        .currency(Currency.KMF.name())
                        .balance(loanTotalAmount)
                        .status(Status.ACTIVE.code())
                        .branch(loanRequestDto.getBranch())
                        .initiatedBy(Helper.getAuthenticated().getName())
                        .build());

        //TODO: create loan
        loanService.save(
                LoanDto.builder()
                        .loanType(loanRequestDto.getLoanType())
                        .account(savedLoanAccount)
                        .issuedAmount(loanRequestDto.getRequestedAmount())
                        .totalAmount(loanTotalAmount)
                        .borrower(borrower)
                        .associatedAccount(loanRequestDto.getAssociatedAccount())
                        .currency(Currency.KMF.name())
                        .duration(loanRequestDto.getDuration())
                        .interestRate(loanRequestDto.getInterestRate())
                        .applicationFees(loanRequestDto.getApplicationFees())
                        .reason(loanRequestDto.getReason())
                        .initiatedBy(Helper.getAuthenticated().getName())
                        .status(Status.ACTIVE.code())
                        .branch(loanRequestDto.getBranch())
                        .build());

        //TODO: update Loan Request info and save it
        loanRequestDto.setStatus(Status.APPROVED.code());
        loanRequestDto.setFailureReason("Approved");
        loanRequestDto.setUpdatedBy(Helper.getAuthenticated().getName());
        loanRequestService.save(loanRequestDto);

        //TODO: credit associated account and apply application fees
        AccountDto associatedAccount = loanRequestDto.getAssociatedAccount();
        accountService.credit(associatedAccount, loanRequestDto.getRequestedAmount());

        Fee fee = new Fee(loanRequestDto.getBranch(), loanRequestDto.getApplicationFees(), "Loan");
        feeService.apply(associatedAccount, LedgerType.LOAN, fee);
    }

    @Override
    public void reject(LoanRequestDto loanRequestDto, String remarks) {

        loanRequestDto.setStatus(Status.REJECTED.code());
        loanRequestDto.setFailureReason(remarks);
        loanRequestDto.setUpdatedBy(Helper.getAuthenticated().getName());

        loanRequestService.save(loanRequestDto);
    }

    public void repay(Long loanId, double amount) {

        LoanDto loanDto = loanService.findById(loanId);
        AccountDto loanAccount = loanDto.getAccount();
        AccountDto associatedAccount = loanDto.getAssociatedAccount();
        double remainingAmount = loanAccount.getBalance();

        //ensure that loan is active
        if(!Helper.isActiveStatedObject(loanDto))
            throw new IllegalOperationException("Loan must be active");

        //ensure that the requested loan amount does not exceed the available balance in the associated account
        if(amount > associatedAccount.getBalance())
            throw new IllegalOperationException("The requested amount exceeds the available balance in the associated account");


        double amountToDebit = Math.min(amount, remainingAmount);

        // Create and apply a fee to the associated account under the LOAN ledger,
        // Finally, debit the specified loan account with the repayment amount.
        Fee fee = new Fee(loanDto.getBranch(), amountToDebit, "Repay Loan");
        feeService.apply(associatedAccount, LedgerType.LOAN, fee);
        accountService.debit(loanAccount, amountToDebit);

        // Check if the loan is fully repaid after this transaction.
        // if it's true, we make it as COMPLETED and close his dedicated account
        if (amountToDebit == remainingAmount) {

            loanDto.setStatus(Status.COMPLETED.code());

            loanAccount.setStatus(Status.CLOSED.code());
            loanAccount.setUpdatedBy(Helper.getAuthenticated().getName());
            accountService.save(loanAccount);
        }

        loanDto.setUpdatedBy(Helper.getAuthenticated().getName());
        loanDto.setFailureReason("Repay loan");
        loanService.save(loanDto);
    }
}
