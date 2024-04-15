package com.dabel.service.cheque;

import com.dabel.dto.ChequeRequestDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.PostChequeRequestDto;
import com.dabel.dto.TrunkDto;
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

    /**
     * For cheque requests
     */

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

    public List<ChequeRequestDto> findAllChequeRequests() {
        return chequeRequestService.findAll();
    }

    public ChequeRequestDto findRequestById(Long requestId) {
        return chequeRequestService.findById(requestId);
    }
}
