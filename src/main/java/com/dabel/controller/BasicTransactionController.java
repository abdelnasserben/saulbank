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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BasicTransactionController implements PageTitleConfig {

    private final TransactionFacadeService transactionFacadeService;
    private final BranchFacadeService branchFacadeService;
    private final AccountFacadeService accountFacadeService;

    public BasicTransactionController(TransactionFacadeService transactionFacadeService, BranchFacadeService branchFacadeService, AccountFacadeService accountFacadeService) {
        this.transactionFacadeService = transactionFacadeService;
        this.branchFacadeService = branchFacadeService;
        this.accountFacadeService = accountFacadeService;
    }

    @GetMapping(value = App.Endpoint.TRANSACTION_ROOT)
    public String listingBasicTransaction(Model model) {
        configPageTitle(model, App.Menu.Transaction.Basics.ROOT);
        model.addAttribute("transactions", StatedObjectFormatter.format(transactionFacadeService.findAll()));

        return App.View.TRANSACTION_LIST;
    }

    @GetMapping(value = App.Endpoint.TRANSACTION_INIT)
    public String initBasicTransaction(Model model, TransactionDto transactionDto) {
        configPageTitle(model, App.Menu.Transaction.Basics.INIT);

        return App.View.TRANSACTION_INIT;
    }

    @PostMapping(value = App.Endpoint.TRANSACTION_INIT)
    public String initBasicTransaction(Model model, @Valid TransactionDto transactionDto, BindingResult binding,
                                       @RequestParam String accountNumber,
                                       RedirectAttributes redirect) {

        if(binding.hasErrors()) {
            configPageTitle(model, App.Menu.Transaction.Basics.INIT);
            model.addAttribute(App.MessageTag.ERROR, "Invalid information!");

            return App.View.TRANSACTION_INIT;
        }

        //TODO: set transaction account
        AccountDto accountDto = accountFacadeService.findCustomerAccountByNumber(accountNumber).getAccount();
        transactionDto.setInitiatorAccount(accountDto);

        //TODO: set branch - We'll replace this automatically by user authenticated
        BranchDto branchDto = branchFacadeService.findById(1L);
        transactionDto.setBranch(branchDto);

        transactionFacadeService.init(transactionDto);
        redirect.addFlashAttribute(App.MessageTag.SUCCESS, transactionDto.getTransactionType() + " successfully initiated.");

        return "redirect:" + App.Endpoint.TRANSACTION_INIT;
    }


    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{App.Menu.Transaction.MENU, App.Menu.Transaction.Basics.SUB_MENU};
    }
}
