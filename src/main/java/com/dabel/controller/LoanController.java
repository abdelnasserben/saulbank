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

    private static final String SUCCESS_LOAN_PAY_MESSAGE = "Loan successfully paid";
    private static final String SUCCESS_LOAN_REQUEST_MESSAGE = "Loan successfully requested";
    private static final String SUCCESS_LOAN_APPROVE_MESSAGE = "Loan successfully approved!";
    private static final String SUCCESS_LOAN_REJECT_MESSAGE = "Loan successfully rejected!";
    private static final String ERROR_INVALID_INFO_MESSAGE = "Invalid information!";
    private static final String ERROR_REJECT_REASON_MESSAGE = "Reject reason is mandatory!";
    private static final String ERROR_NEGATIVE_AMOUNT_MESSAGE = "Amount must be positive";

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
    public String listLoans(Model model) {
        configPageTitle(model, Web.Menu.Loan.ROOT);
        model.addAttribute("loans", StatedObjectFormatter.format(loanFacadeService.getAllLoans()));

        return Web.View.LOANS;
    }

    @GetMapping(value = Web.Endpoint.LOANS + "/{loanId}")
    public String showLoanDetails(@PathVariable Long loanId, Model model) {

        LoanDto loanDto = loanFacadeService.getLoanById(loanId);

        configPageTitle(model, "Loan Details");
        model.addAttribute("loan", StatedObjectFormatter.format(loanDto));
        return Web.View.LOAN_DETAILS;
    }

    @PostMapping(value = Web.Endpoint.LOANS + "/{loanId}")
    public String repayLoan(@PathVariable Long loanId, Model model,
                            @RequestParam(defaultValue = "0") double amount,
                            RedirectAttributes redirect) {

        if(amount <= 0)
            throw new IllegalOperationException(ERROR_NEGATIVE_AMOUNT_MESSAGE);

        loanFacadeService.repayLoan(loanId, amount);

        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, SUCCESS_LOAN_PAY_MESSAGE);
        return "redirect:" + Web.Endpoint.LOANS + "/" + loanId;
    }


    /*** FOR LOAN REQUESTS ***/

    @GetMapping(value = Web.Endpoint.LOAN_REQUESTS)
    public String listLoanRequests(Model model) {

        configPageTitle(model, Web.Menu.Loan.REQUESTS);
        model.addAttribute("loanRequests", StatedObjectFormatter.format(loanFacadeService.getAllLoanRequests()));

        return Web.View.LOAN_REQUESTS;
    }

    @GetMapping(value = Web.Endpoint.LOAN_REQUEST)
    public String requestLoan(Model model, LoanRequestDto loanRequestDto) {
        configPageTitle(model, Web.Menu.Loan.REQUEST);

        return Web.View.LOAN_REQUEST;
    }

    @PostMapping(value = Web.Endpoint.LOAN_REQUEST)
    public String submitLoanRequest(Model model, @Valid LoanRequestDto loanRequestDto, BindingResult bindingResult,
                                    @RequestParam String customerIdentityNumber,
                                    @RequestParam String beneficiaryAccount,
                                    RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors() || customerIdentityNumber.isBlank() || beneficiaryAccount.isBlank()) {
            return handleLoanRequestError(model);
        }

        initializeLoanRequest(loanRequestDto, customerIdentityNumber, beneficiaryAccount);
        loanFacadeService.initLoanRequest(loanRequestDto);
        redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, SUCCESS_LOAN_REQUEST_MESSAGE);

        return "redirect:" + Web.Endpoint.LOAN_REQUEST;
    }

    @GetMapping(value = Web.Endpoint.LOAN_REQUESTS + "/{requestId}")
    public String showLoanRequestDetails(@PathVariable Long requestId, Model model) {

        LoanRequestDto requestDto = loanFacadeService.getLoanRequestById(requestId);

        configPageTitle(model, "Request Details");
        model.addAttribute("requestDto", StatedObjectFormatter.format(requestDto));
        return Web.View.LOAN_REQUEST_DETAILS;
    }

    @PostMapping(value = Web.Endpoint.LOAN_REQUESTS_APPROVE + "/{requestId}")
    public String approveLoanRequest(@PathVariable Long requestId, RedirectAttributes redirectAttributes) {

        loanFacadeService.approveLoanRequest(requestId);
        redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, SUCCESS_LOAN_APPROVE_MESSAGE);

        return redirectToLoanRequestDetails(requestId);
    }

    @PostMapping(value = Web.Endpoint.LOAN_REQUESTS_REJECT + "/{requestId}")
    public String rejectLoanRequest(@PathVariable Long requestId, @RequestParam String rejectReason, RedirectAttributes redirectAttributes) {

        if(rejectReason.isBlank())
            redirectAttributes.addFlashAttribute(Web.MessageTag.ERROR, ERROR_REJECT_REASON_MESSAGE);
        else {
            redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, SUCCESS_LOAN_REJECT_MESSAGE);
            loanFacadeService.rejectLoanRequest(requestId, rejectReason);
        }

        return redirectToLoanRequestDetails(requestId);
    }

    private void initializeLoanRequest(LoanRequestDto loanRequestDto, String customerIdentityNumber, String beneficiaryAccount) {
        CustomerDto borrower = customerFacadeService.getByIdentityNumber(customerIdentityNumber);
        AccountDto associatedAccount = accountFacadeService.getTrunkByCustomerAndNumber(borrower, beneficiaryAccount).getAccount();

        loanRequestDto.setBorrower(borrower);
        loanRequestDto.setAssociatedAccount(associatedAccount);
        loanRequestDto.setBranch(userService.getAuthenticated().getBranch());
    }

    private String handleLoanRequestError(Model model) {
        model.addAttribute("pageTitle", Web.Menu.Loan.REQUEST);
        model.addAttribute(Web.MessageTag.ERROR, ERROR_INVALID_INFO_MESSAGE);
        return Web.View.LOAN_REQUEST;
    }

    private String redirectToLoanRequestDetails(Long requestId) {
        return "redirect:" + Web.Endpoint.LOAN_REQUESTS + "/" + requestId;
    }


    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Loan.MENU, null};
    }
}
