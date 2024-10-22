package com.dabel.service.cheque;

import com.dabel.app.Helper;
import com.dabel.constant.SourceType;
import com.dabel.constant.Status;
import com.dabel.constant.TransactionType;
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
        TrunkDto trunkDto = accountService.findTrunk(customerDto, postChequeRequestDto.getAccountNumber());
        ChequeRequestDto chequeRequestDto = ChequeRequestDto.builder()
                .trunk(trunkDto)
                .branch(userService.getAuthenticated().getBranch())
                .build();
        chequeRequestContext.setContext(trunkDto.getAccount().getAccountType()).init(chequeRequestDto);
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

        ChequeDto chequeDto = chequeService.findById(chequeId);

        if(Helper.isActiveStatedObject(chequeDto))
            throw new IllegalOperationException("Cheque already active");

        chequeDto.setStatus(Status.ACTIVE.code());
        chequeDto.setFailureReason("Activation");
        chequeDto.setUpdatedBy(Helper.getAuthenticated().getName());

        chequeService.save(chequeDto);
    }

    public void deactivateCheque(Long chequeId, String remarks) {

        ChequeDto chequeDto = chequeService.findById(chequeId);

        if(!Helper.isActiveStatedObject(chequeDto))
            throw new IllegalOperationException("Unable to deactivate an inactive cheque");

        chequeDto.setStatus(Status.INACTIVE.code());
        chequeDto.setFailureReason(remarks);
        chequeDto.setUpdatedBy(Helper.getAuthenticated().getName());

        chequeService.save(chequeDto);
    }

    public ChequeDto findChequeByNumber(String chequeNumber) {
        return chequeService.findByChequeNumber(chequeNumber);
    }

    public TransactionDto initPay(PostChequeDto postChequeDto) {

        ChequeDto chequeDto = chequeService.findByChequeNumber(postChequeDto.getChequeNumber());

        if(!Helper.isActiveStatedObject(chequeDto))
            throw new IllegalOperationException("Cheque must be active");

        AccountDto receiverAccount = accountService.findByNumber(postChequeDto.getBeneficiaryAccountNumber());

        return TransactionDto.builder()
                .initiatorAccount(chequeDto.getTrunk().getAccount())
                .receiverAccount(receiverAccount)
                .transactionType(TransactionType.CHEQUE_PAYMENT.name())
                .currency(chequeDto.getTrunk().getAccount().getCurrency())
                .amount(postChequeDto.getAmount())
                .customerIdentity(chequeDto.getTrunk().getCustomer().getIdentityNumber())
                .customerFullName(chequeDto.getTrunk().getCustomer().getFirstName() + " " + chequeDto.getTrunk().getCustomer().getLastName())
                .reason(postChequeDto.getReason())
                .sourceType(SourceType.CHEQUE.name())
                .sourceValue(chequeDto.getChequeNumber())
                .branch(userService.getAuthenticated().getBranch())
                .initiatedBy(Helper.getAuthenticated().getName())
                .build();
    }
}
