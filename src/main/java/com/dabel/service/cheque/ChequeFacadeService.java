package com.dabel.service.cheque;

import com.dabel.app.Helper;
import com.dabel.constant.*;
import com.dabel.dto.*;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.account.AccountService;
import com.dabel.service.customer.CustomerService;
import com.dabel.service.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChequeFacadeService {

    private final ChequeService chequeService;
    private final ChequeRequestService chequeRequestService;
    private final ChequeRequestContext chequeRequestContext;
    private final AccountService accountService;
    private final CustomerService customerService;
    private final UserService userService;

    public ChequeFacadeService(ChequeService chequeService, ChequeRequestService chequeRequestService, ChequeRequestContext chequeRequestContext, AccountService accountService, CustomerService customerService, UserService userService) {
        this.chequeService = chequeService;
        this.chequeRequestService = chequeRequestService;
        this.chequeRequestContext = chequeRequestContext;
        this.accountService = accountService;
        this.customerService = customerService;
        this.userService = userService;
    }

    public ChequeDto saveCheque(ChequeDto chequeDto) {
        return chequeService.save(chequeDto);
    }

    public List<ChequeDto> findAllCheques() {
        return chequeService.findAll();
    }

    public ChequeDto findChequeById(Long chequeId) {
        return chequeService.findById(chequeId);
    }


    /**
     * For cheque requests
     */

    public List<ChequeRequestDto> findAllRequests() {
        return chequeRequestService.findAll();
    }

    public ChequeRequestDto findRequestById(Long requestId) {
        return chequeRequestService.findById(requestId);
    }

    public void sendRequest(PostChequeRequestDto postChequeRequestDto) {

        CustomerDto customerDto = customerService.findByIdentity(postChequeRequestDto.getCustomerIdentityNumber());
        TrunkDto trunkDto = accountService.findTrunkByCustomerAndAccountNumber(customerDto, postChequeRequestDto.getAccountNumber());

        AccountType accountType = AccountType.valueOf(trunkDto.getAccount().getAccountType());
        double applicationFees = accountType.equals(AccountType.BUSINESS) ? BankFees.Basic.BUSINESS_CHEQUE : BankFees.Basic.SAVING_CHEQUE;

        ChequeRequestDto chequeRequestDto = ChequeRequestDto.builder()
                .trunk(trunkDto)
                .applicationFees(applicationFees)
                .branch(userService.getAuthenticated().getBranch())
                .build();
        chequeRequestContext.setContext(accountType.name()).init(chequeRequestDto);
    }

    public void approveRequest(Long requestId) {
        ChequeRequestDto requestDto = chequeRequestService.findById(requestId);
        chequeRequestContext.setContext(requestDto.getTrunk().getAccount().getAccountType()).approve(requestDto);
    }

    public void rejectRequest(Long requestId, String remarks) {
        ChequeRequestDto requestDto = chequeRequestService.findById(requestId);
        chequeRequestContext.setContext(requestDto.getTrunk().getAccount().getAccountType()).reject(requestDto, remarks);
    }

    public void activateCheque(Long chequeId) {
        updateChequeStatus(chequeId, Status.ACTIVE, "Activation");
    }

    public void deactivateCheque(Long chequeId, String remarks) {
        updateChequeStatus(chequeId, Status.INACTIVE, remarks);
    }

    public ChequeDto findChequeByNumber(String chequeNumber) {
        return chequeService.findByChequeNumber(chequeNumber);
    }

    public TransactionDto initPay(PostChequeDto postChequeDto) {

        ChequeDto chequeDto = chequeService.findByChequeNumber(postChequeDto.getChequeNumber());
        validateActiveCheque(chequeDto);

        //TODO: Retrieve the receiver account
        AccountDto receiverAccount = accountService.findAccountByNumber(postChequeDto.getBeneficiaryAccountNumber());

        //TODO: Retrieve cheque account and customer details
        AccountDto initiatorAccount = chequeDto.getTrunk().getAccount();
        String customerIdentity = chequeDto.getTrunk().getCustomer().getIdentityNumber();
        String customerFullName = chequeDto.getTrunk().getCustomer().getFirstName() + " " + chequeDto.getTrunk().getCustomer().getLastName();

        //TODO: Construct and return the transaction
        return TransactionDto.builder()
                .initiatorAccount(initiatorAccount)
                .receiverAccount(receiverAccount)
                .transactionType(TransactionType.CHEQUE_PAYMENT.name())
                .currency(initiatorAccount.getCurrency())
                .amount(postChequeDto.getAmount())
                .customerIdentity(customerIdentity)
                .customerFullName(customerFullName)
                .reason(postChequeDto.getReason())
                .sourceType(SourceType.CHEQUE.name())
                .sourceValue(chequeDto.getChequeNumber())
                .branch(userService.getAuthenticated().getBranch())
                .initiatedBy(Helper.getAuthenticated().getName())
                .build();
    }

    private void validateActiveCheque(ChequeDto chequeDto) {
        if (!Helper.isActiveStatedObject(chequeDto)) {
            throw new IllegalOperationException("Cheque must be active.");
        }
    }

    /**
     * Updates the status of a cheque based on the requested action.
     *
     * @param chequeId The ID of the cheque to update.
     * @param newStatus The target status for the cheque, either ACTIVE or INACTIVE.
     * @param reason The reason for the status update.
     * @throws IllegalOperationException if attempting to activate an already active cheque
     *                                   or deactivate an inactive cheque.
     */
    private void updateChequeStatus(Long chequeId, Status newStatus, String reason) {
        ChequeDto chequeDto = chequeService.findById(chequeId);
        boolean isCurrentlyActive = Helper.isActiveStatedObject(chequeDto);

        //TODO: Check the current status against the target status
        if (newStatus == Status.ACTIVE && isCurrentlyActive) {
            throw new IllegalOperationException("Cheque already active, cannot activate again.");
        } else if (newStatus == Status.INACTIVE && !isCurrentlyActive) {
            throw new IllegalOperationException("Cheque is inactive, cannot deactivate again.");
        }


        chequeDto.setStatus(newStatus.code());
        chequeDto.setFailureReason(reason);
        chequeDto.setUpdatedBy(Helper.getAuthenticated().getName());

        chequeService.save(chequeDto);
    }
}
