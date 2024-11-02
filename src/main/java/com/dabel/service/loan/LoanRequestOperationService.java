package com.dabel.service.loan;

import com.dabel.app.Fee;
import com.dabel.app.Helper;
import com.dabel.constant.*;
import com.dabel.dto.*;
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
        validateBorrowerAndAccount(loanRequestDto);

        loanRequestDto.setStatus(Status.PENDING.code());
        loanRequestDto.setInitiatedBy(Helper.getAuthenticated().getName());
        loanRequestService.save(loanRequestDto);
    }

    private void validateBorrowerAndAccount(LoanRequestDto requestDto) {
        if (!Helper.isActiveStatedObject(requestDto.getBorrower()) || !Helper.isActiveStatedObject(requestDto.getAssociatedAccount())) {
            throw new IllegalOperationException("Borrower and their associated account must be active.");
        }
    }

    @Override
    public void approve(LoanRequestDto loanRequestDto) {

        double totalLoanAmount = Helper.calculateTotalAmountOfLoan(loanRequestDto.getRequestedAmount(), loanRequestDto.getInterestRate());
        CustomerDto borrower = loanRequestDto.getBorrower();

        AccountDto loanAccount = createLoanAccount(borrower, totalLoanAmount, loanRequestDto);
        createLoan(loanRequestDto, borrower, totalLoanAmount, loanAccount);

        updateLoanRequestStatus(loanRequestDto, Status.APPROVED.code(), "Approved");

        creditAssociatedAccount(loanRequestDto);
    }

    private AccountDto createLoanAccount(CustomerDto borrower, double totalLoanAmount, LoanRequestDto loanRequestDto) {
        AccountDto loanAccount = AccountDto.builder()
                .accountName(String.format("%s %s", borrower.getFirstName(), borrower.getLastName()))
                .accountNumber(Helper.generateAccountNumber())
                .accountType(AccountType.LOAN.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .currency(Currency.KMF.name())
                .balance(totalLoanAmount)
                .status(Status.ACTIVE.code())
                .branch(loanRequestDto.getBranch())
                .initiatedBy(Helper.getAuthenticated().getName())
                .build();

        return accountService.saveAccount(loanAccount);
    }

    private void createLoan(LoanRequestDto loanRequestDto, CustomerDto borrower, double totalLoanAmount, AccountDto loanAccount) {
        LoanDto loanDto = LoanDto.builder()
                .loanType(loanRequestDto.getLoanType())
                .account(loanAccount)
                .issuedAmount(loanRequestDto.getRequestedAmount())
                .totalAmount(totalLoanAmount)
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
                .build();

        loanService.save(loanDto);
    }

    private void updateLoanRequestStatus(LoanRequestDto requestDto, String status, String failureReason) {
        requestDto.setStatus(status);
        requestDto.setFailureReason(failureReason);
        requestDto.setUpdatedBy(Helper.getAuthenticated().getName());
        loanRequestService.save(requestDto);
    }

    private void creditAssociatedAccount(LoanRequestDto loanRequestDto) {
        AccountDto associatedAccount = loanRequestDto.getAssociatedAccount();
        accountService.creditAccount(associatedAccount, loanRequestDto.getRequestedAmount());

        // Apply the loan application fees to the associated account in the specified branch.
        applyFees(loanRequestDto.getBranch(), associatedAccount, loanRequestDto.getApplicationFees(), "Applying loan application fees");
    }

    @Override
    public void reject(LoanRequestDto loanRequestDto, String remarks) {
        updateLoanRequestStatus(loanRequestDto, Status.REJECTED.code(), remarks);
    }

    public void repay(Long loanId, double amount) {
        LoanDto loan = loanService.findById(loanId);
        validateLoan(loan); //ensure that loan is active

        AccountDto loanAccount = loan.getAccount();
        AccountDto associatedAccount = loan.getAssociatedAccount();
        double remainingAmount = loanAccount.getBalance();

        //ensure that the requested loan amount does not exceed the available balance in the associated account
        validateRepaymentAmount(amount, associatedAccount);

        double amountToDebit = Math.min(amount, remainingAmount);

        // The loan repayment is treated as a fee.
        // We debit the associated account and credit the branch ledger.
        applyFees(loan.getBranch(), associatedAccount, amountToDebit, "Repay Loan");

        // We simply reduce the balance of the associated account by debiting the owed amount.
        accountService.debitAccount(loanAccount, amountToDebit);

        if (amountToDebit == remainingAmount) {
            completeLoan(loan);
        }

        updateLoanStatus(loan);
    }

    private void validateLoan(LoanDto loanDto) {
        if (!Helper.isActiveStatedObject(loanDto)) {
            throw new IllegalOperationException("Loan must be active.");
        }
    }

    private void validateRepaymentAmount(double amount, AccountDto associatedAccount) {
        if (amount > associatedAccount.getBalance()) {
            throw new IllegalOperationException("The requested amount exceeds the available balance in the associated account.");
        }
    }

    private void completeLoan(LoanDto loan) {
        loan.setStatus(Status.COMPLETED.code());

        AccountDto loanAccount = loan.getAccount();
        loanAccount.setStatus(Status.CLOSED.code());
        loanAccount.setUpdatedBy(Helper.getAuthenticated().getName());
        accountService.saveAccount(loanAccount);
    }

    private void updateLoanStatus(LoanDto loanDto) {
        loanDto.setUpdatedBy(Helper.getAuthenticated().getName());
        loanDto.setFailureReason("Repay loan");
        loanService.save(loanDto);
    }

    private void applyFees(BranchDto branchDto, AccountDto accountDto, double amountToDebit, String remarks) {
        Fee fee = new Fee(branchDto, amountToDebit, remarks);
        feeService.apply(accountDto, LedgerType.LOAN, fee);
    }
}
