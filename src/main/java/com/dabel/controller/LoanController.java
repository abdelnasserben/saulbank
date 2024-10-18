package com.dabel.controller;

import com.dabel.app.StatedObjectFormatter;
import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.Web;
import com.dabel.dto.AccountDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.LoanDto;
import com.dabel.dto.LoanRequestDto;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.customer.CustomerFacadeService;
import com.dabel.service.loan.LoanFacadeService;
import com.dabel.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoanController implements PageTitleConfig {

    private final LoanFacadeService loanFacadeService;
    private final CustomerFacadeService customerFacadeService;
    private final AccountFacadeService accountFacadeService;
    private final UserService userService;

    @Autowired
    public LoanController(LoanFacadeService loanFacadeService, CustomerFacadeService customerFacadeService, AccountFacadeService accountFacadeService, UserService userService) {
        this.loanFacadeService = loanFacadeService;
        this.customerFacadeService = customerFacadeService;
        this.accountFacadeService = accountFacadeService;
        this.userService = userService;
    }

    /*** FOR LOAN REQUESTS ***/

    @GetMapping(value = Web.Endpoint.LOANS)
    public String listingLoans(Model model) {
        configPageTitle(model, Web.Menu.Loan.ROOT);
        model.addAttribute("loans", StatedObjectFormatter.format(loanFacadeService.findAll()));

        return Web.View.LOANS;
    }

    @GetMapping(value = Web.Endpoint.LOANS + "/{loanId}")
    public String loanDetails(@PathVariable Long loanId, Model model) {

        LoanDto loanDto = loanFacadeService.findById(loanId);

        configPageTitle(model, "Loan Details");
        model.addAttribute("loan", StatedObjectFormatter.format(loanDto));
        return Web.View.LOAN_DETAILS;
    }

    @PostMapping(value = Web.Endpoint.LOANS + "/{loanId}")
    public String payLoan(@PathVariable Long loanId, Model model,
                          @RequestParam(defaultValue = "0") double amount,
                          RedirectAttributes redirect) {

        if(amount <= 0)
            throw new IllegalOperationException("Amount must be positive");

        loanFacadeService.repay(loanId, amount);

        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Loan successfully payed");
        return "redirect:" + Web.Endpoint.LOANS + "/" + loanId;
    }


    /*** FOR LOAN REQUESTS ***/

    @GetMapping(value = Web.Endpoint.LOAN_REQUESTS)
    public String listingRequests(Model model) {

        configPageTitle(model, Web.Menu.Loan.REQUESTS);
        model.addAttribute("loanRequests", StatedObjectFormatter.format(loanFacadeService.findAllRequests()));

        return Web.View.LOAN_REQUESTS;
    }

    @GetMapping(value = Web.Endpoint.LOAN_REQUEST)
    public String requestLoan(Model model, LoanRequestDto loanRequestDto) {
        configPageTitle(model, Web.Menu.Loan.REQUEST);

        return Web.View.LOAN_REQUEST;
    }

    @PostMapping(value = Web.Endpoint.LOAN_REQUEST)
    public String requestLoan(Model model, @Valid LoanRequestDto loanRequestDto, BindingResult binding,
                              @RequestParam String customerIdentityNumber,
                              @RequestParam String beneficiaryAccount,
                              RedirectAttributes redirect) {

        if(binding.hasErrors() || customerIdentityNumber.isEmpty() || customerIdentityNumber.isBlank() || beneficiaryAccount.isEmpty() || beneficiaryAccount.isBlank() ) {
            configPageTitle(model, Web.Menu.Loan.REQUEST);
            model.addAttribute(Web.MessageTag.ERROR, "Invalid information !");
            return Web.View.LOAN_REQUEST;
        }

        //TODO: setup loan request info [borrower and his associated account, branch]
        CustomerDto borrower = customerFacadeService.findByIdentity(customerIdentityNumber);
        AccountDto associatedAccount = accountFacadeService.findTrunkByCustomerAndAccountNumber(borrower, beneficiaryAccount).getAccount();
        loanRequestDto.setBorrower(borrower);
        loanRequestDto.setAssociatedAccount(associatedAccount);

        loanRequestDto.setBranch(userService.getAuthenticated().getBranch());

        loanFacadeService.initRequest(loanRequestDto);

        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Loan successfully requested");
        return "redirect:" + Web.Endpoint.LOAN_REQUEST;
    }

    @GetMapping(value = Web.Endpoint.LOAN_REQUESTS + "/{requestId}")
    public String requestDetails(@PathVariable Long requestId, Model model) {

        LoanRequestDto requestDto = loanFacadeService.findRequestById(requestId);

        configPageTitle(model, "Request Details");
        model.addAttribute("requestDto", StatedObjectFormatter.format(requestDto));
        return Web.View.LOAN_REQUEST_DETAILS;
    }

    @PostMapping(value = Web.Endpoint.LOAN_REQUESTS_APPROVE + "/{requestId}")
    public String approveLoanRequest(@PathVariable Long requestId, RedirectAttributes redirect) {

        loanFacadeService.approveRequest(requestId);
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Loan successfully approved!");

        return "redirect:" + Web.Endpoint.LOAN_REQUESTS + "/" + requestId;
    }

    @PostMapping(value = Web.Endpoint.LOAN_REQUESTS_REJECT + "/{requestId}")
    public String rejectLoanRequest(@PathVariable Long requestId, @RequestParam String rejectReason, RedirectAttributes redirect) {

        if(rejectReason.isBlank())
            redirect.addFlashAttribute(Web.MessageTag.ERROR, "Reject reason is mandatory!");
        else {
            redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Loan successfully rejected!");
            loanFacadeService.rejectRequest(requestId, rejectReason);
        }

        return "redirect:" + Web.Endpoint.LOAN_REQUESTS + "/" + requestId;
    }


    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Loan.MENU, null};
    }
}
