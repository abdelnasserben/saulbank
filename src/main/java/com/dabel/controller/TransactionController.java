package com.dabel.controller;

import com.dabel.app.StatedObjectFormatter;
import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.App;
import com.dabel.dto.AccountDto;
import com.dabel.dto.BranchDto;
import com.dabel.dto.TransactionDto;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.branch.BranchFacadeService;
import com.dabel.service.transaction.TransactionFacadeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TransactionController implements PageTitleConfig {

    private final TransactionFacadeService transactionFacadeService;
    private final BranchFacadeService branchFacadeService;
    private final AccountFacadeService accountFacadeService;

    public TransactionController(TransactionFacadeService transactionFacadeService, BranchFacadeService branchFacadeService, AccountFacadeService accountFacadeService) {
        this.transactionFacadeService = transactionFacadeService;
        this.branchFacadeService = branchFacadeService;
        this.accountFacadeService = accountFacadeService;
    }

    @GetMapping(value = App.Endpoint.TRANSACTION_ROOT)
    public String listingTransaction(Model model) {
        configPageTitle(model, App.Menu.Transaction.ROOT);
        model.addAttribute("transactions", StatedObjectFormatter.format(transactionFacadeService.findAll()));

        return App.View.TRANSACTION_LIST;
    }

    @GetMapping(value = App.Endpoint.TRANSACTION_ROOT + "/{transactionId}")
    public String transactionDetails(@PathVariable Long transactionId, Model model) {

        TransactionDto transactionDto = transactionFacadeService.findById(transactionId);

        model.addAttribute("transaction", StatedObjectFormatter.format(transactionDto));
        configPageTitle(model, "Transaction Details");
        return App.View.TRANSACTION_DETAILS;
    }

    @GetMapping(value = App.Endpoint.TRANSACTION_INIT)
    public String initTransaction(Model model, TransactionDto transactionDto) {
        configPageTitle(model, App.Menu.Transaction.INIT);

        return App.View.TRANSACTION_INIT;
    }

    @PostMapping(value = App.Endpoint.TRANSACTION_INIT)
    public String initTransaction(Model model, @Valid TransactionDto transactionDto, BindingResult binding,
                                  @RequestParam String accountNumber,
                                  RedirectAttributes redirect) {

        if(binding.hasErrors()) {
            configPageTitle(model, App.Menu.Transaction.INIT);
            model.addAttribute(App.MessageTag.ERROR, "Invalid information!");

            return App.View.TRANSACTION_INIT;
        }

        //TODO: set transaction account
        AccountDto accountDto = accountFacadeService.findCustomerAccountByNumber(accountNumber).getAccount();
        transactionDto.setInitiatorAccount(accountDto);

        //TODO: set branch - We'll replace this automatically by user authenticated
        BranchDto branchDto = branchFacadeService.findById(1L);
        transactionDto.setBranch(branchDto);
        transactionDto.setSourceValue(branchDto.getBranchName());

        transactionFacadeService.init(transactionDto);
        redirect.addFlashAttribute(App.MessageTag.SUCCESS, transactionDto.getTransactionType() + " successfully initiated.");

        return "redirect:" + App.Endpoint.TRANSACTION_INIT;
    }

    @GetMapping(value = App.Endpoint.TRANSACTION_APPROVE + "/{transactionId}")
    public String approveTransaction(@PathVariable Long transactionId, RedirectAttributes redirect) {

        transactionFacadeService.approve(transactionId);
        redirect.addFlashAttribute(App.MessageTag.SUCCESS, "Transaction successfully approved!");

        return "redirect:" + App.Endpoint.TRANSACTION_ROOT + "/" + transactionId;
    }

    @PostMapping(value = App.Endpoint.TRANSACTION_REJECT + "/{transactionId}")
    public String rejectTransaction(@PathVariable Long transactionId, @RequestParam String rejectReason, RedirectAttributes redirect) {

        if(rejectReason.isBlank())
            redirect.addFlashAttribute(App.MessageTag.ERROR, "Reject reason is mandatory!");
        else {
            redirect.addFlashAttribute(App.MessageTag.SUCCESS, "Transaction successfully rejected!");
            transactionFacadeService.reject(transactionId, rejectReason);
        }

        return "redirect:" + App.Endpoint.TRANSACTION_ROOT + "/" + transactionId;
    }


    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{App.Menu.Transaction.MENU, null};
    }
}
