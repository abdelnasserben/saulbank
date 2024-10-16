package com.dabel.controller;

import com.dabel.app.StatedObjectFormatter;
import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.Web;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.LoanDto;
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
    private final UserService userService;

    @Autowired
    public LoanController(LoanFacadeService loanFacadeService, CustomerFacadeService customerFacadeService, UserService userService) {
        this.loanFacadeService = loanFacadeService;
        this.customerFacadeService = customerFacadeService;
        this.userService = userService;
    }

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

    @GetMapping(value = Web.Endpoint.LOAN_INIT)
    public String initLoan(Model model, LoanDto loanDto) {
        configPageTitle(model, Web.Menu.Loan.INIT);

        return Web.View.LOAN_INIT;
    }

    @PostMapping(value = Web.Endpoint.LOAN_INIT)
    public String initLoan(Model model, @Valid LoanDto loanDto, BindingResult binding,
                           @RequestParam String customerIdentityNumber,
                           RedirectAttributes redirect) {

        if(binding.hasErrors() || customerIdentityNumber.isEmpty() || customerIdentityNumber.isBlank()) {
            configPageTitle(model, Web.Menu.Loan.INIT);
            model.addAttribute(Web.MessageTag.ERROR, "Invalid information !");
            return Web.View.LOAN_INIT;
        }

        //TODO: set customer
        CustomerDto customerDto = customerFacadeService.findByIdentity(customerIdentityNumber);
        loanDto.setBorrower(customerDto);

        loanDto.setBranch(userService.getAuthenticated().getBranch());

        loanFacadeService.init(loanDto);

        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Loan successfully initiated");
        return "redirect:" + Web.Endpoint.LOAN_INIT;
    }

    @PostMapping(value = Web.Endpoint.LOAN_APPROVE + "/{loanId}")
    public String approveLoan(@PathVariable Long loanId, RedirectAttributes redirect) {

        loanFacadeService.approve(loanId);
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Loan successfully approved!");

        return "redirect:" + Web.Endpoint.LOANS + "/" + loanId;
    }

    @PostMapping(value = Web.Endpoint.LOAN_REJECT + "/{loanId}")
    public String rejectLoan(@PathVariable Long loanId, @RequestParam String rejectReason, RedirectAttributes redirect) {

        if(rejectReason.isBlank())
            redirect.addFlashAttribute(Web.MessageTag.ERROR, "Reject reason is mandatory!");
        else {
            redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Loan successfully rejected!");
            loanFacadeService.reject(loanId, rejectReason);
        }

        return "redirect:" + Web.Endpoint.LOANS + "/" + loanId;
    }


    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Loan.MENU, null};
    }
}
