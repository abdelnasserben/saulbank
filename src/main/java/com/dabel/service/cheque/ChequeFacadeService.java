package com.dabel.service.cheque;

import com.dabel.app.Helper;
import com.dabel.constant.Status;
import com.dabel.dto.*;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.account.AccountService;
import com.dabel.service.customer.CustomerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChequeFacadeService {

    private final ChequeService chequeService;
    private final ChequeRequestService chequeRequestService;
    private final ChequeApplicationContext chequeApplicationContext;
    private final AccountService accountService;
    private final CustomerService customerService;

    public ChequeFacadeService(ChequeService chequeService, ChequeRequestService chequeRequestService, ChequeApplicationContext chequeApplicationContext, AccountService accountService, CustomerService customerService) {
        this.chequeService = chequeService;
        this.chequeRequestService = chequeRequestService;
        this.chequeApplicationContext = chequeApplicationContext;
        this.accountService = accountService;
        this.customerService = customerService;
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
        ChequeRequestDto chequeRequestDto = ChequeRequestDto.builder()
                .trunk(trunkDto)
                .branch(trunkDto.getAccount().getBranch())
                .build();
        chequeApplicationContext.setContext(trunkDto.getAccount().getAccountType()).init(chequeRequestDto);
    }

    public void approveRequest(Long requestId) {
        ChequeRequestDto requestDto = chequeRequestService.findById(requestId);
        chequeApplicationContext.setContext(requestDto.getTrunk().getAccount().getAccountType()).approve(requestDto);
    }

    public void rejectRequest(Long requestId, String remarks) {
        ChequeRequestDto requestDto = chequeRequestService.findById(requestId);
        chequeApplicationContext.setContext(requestDto.getTrunk().getAccount().getAccountType()).reject(requestDto, remarks);
    }

    public void activateCheque(Long chequeId) {

        ChequeDto chequeDto = chequeService.findById(chequeId);

        if(Helper.isActiveStatedObject(chequeDto))
            throw new IllegalOperationException("Cheque already active");

        chequeDto.setStatus(Status.ACTIVE.code());
        chequeDto.setFailureReason("Activation");
        //we'll set update info later...

        chequeService.save(chequeDto);
    }

    public void deactivateCheque(Long chequeId, String remarks) {

        ChequeDto chequeDto = chequeService.findById(chequeId);

        if(!Helper.isActiveStatedObject(chequeDto))
            throw new IllegalOperationException("Unable to deactivate an inactive cheque");

        chequeDto.setStatus(Status.DEACTIVATED.code());
        chequeDto.setFailureReason(remarks);
        //we'll set update info later...

        chequeService.save(chequeDto);
    }
}
