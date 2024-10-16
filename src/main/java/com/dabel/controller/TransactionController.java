package com.dabel.controller;

import com.dabel.app.StatedObjectFormatter;
import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.SourceType;
import com.dabel.constant.TransactionType;
import com.dabel.constant.Web;
import com.dabel.dto.AccountDto;
import com.dabel.dto.BranchDto;
import com.dabel.dto.TransactionDto;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.transaction.TransactionFacadeService;
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
public class TransactionController implements PageTitleConfig {

    private final TransactionFacadeService transactionFacadeService;
    private final AccountFacadeService accountFacadeService;
    private final UserService userService;

    @Autowired
    public TransactionController(TransactionFacadeService transactionFacadeService, AccountFacadeService accountFacadeService, UserService userService) {
        this.transactionFacadeService = transactionFacadeService;
        this.accountFacadeService = accountFacadeService;
        this.userService = userService;
    }

    @GetMapping(value = Web.Endpoint.TRANSACTIONS)
    public String listingTransaction(Model model) {

        configPageTitle(model, Web.Menu.Transaction.ROOT);
        model.addAttribute("transactions", StatedObjectFormatter.format(
                transactionFacadeService.findAll().stream()
                        .filter(transactionDto -> !transactionDto.getTransactionType().equals(TransactionType.FEE.name()))
                        .toList())
        );

        return Web.View.TRANSACTIONS;
    }

    @GetMapping(value = Web.Endpoint.TRANSACTIONS + "/{transactionId}")
    public String transactionDetails(@PathVariable Long transactionId, Model model) {

        TransactionDto transactionDto = transactionFacadeService.findById(transactionId);

        model.addAttribute("transaction", StatedObjectFormatter.format(transactionDto));
        configPageTitle(model, "Transaction Details");
        return Web.View.TRANSACTION_DETAILS;
    }

    @GetMapping(value = Web.Endpoint.TRANSACTION_INIT)
    public String initTransaction(Model model, TransactionDto transactionDto) {
        configPageTitle(model, Web.Menu.Transaction.INIT);

        return Web.View.TRANSACTION_INIT;
    }

    @PostMapping(value = Web.Endpoint.TRANSACTION_INIT)
    public String initTransaction(Model model, @Valid TransactionDto transactionDto, BindingResult binding,
                                  @RequestParam String initiatorAccountNumber,
                                  @RequestParam(name = "receiverAccountNumber", required = false) String receiverAccountNumber,
                                  RedirectAttributes redirect) {

        if(binding.hasErrors() || transactionDto.getTransactionType().equalsIgnoreCase(TransactionType.TRANSFER.name()) && receiverAccountNumber.isEmpty()) {
            configPageTitle(model, Web.Menu.Transaction.INIT);
            model.addAttribute(Web.MessageTag.ERROR, "Invalid information!");

            return Web.View.TRANSACTION_INIT;
        }

        //TODO: set initiator account
        AccountDto initiatorAccount = accountFacadeService.findTrunkByNumber(initiatorAccountNumber).getAccount();
        transactionDto.setInitiatorAccount(initiatorAccount);

        //TODO: set receiver account when transaction is a transfer
        if(transactionDto.getTransactionType().equalsIgnoreCase(TransactionType.TRANSFER.name())) {
            AccountDto receiverAccount = accountFacadeService.findTrunkByNumber(receiverAccountNumber).getAccount();
            transactionDto.setReceiverAccount(receiverAccount);
        }

        //TODO: set branch - We'll replace this automatically by user authenticated
        BranchDto branchDto = userService.getAuthenticated().getBranch();
        transactionDto.setBranch(branchDto);
        transactionDto.setSourceType(SourceType.ONLINE.name());
        transactionDto.setSourceValue(branchDto.getBranchName());

        transactionFacadeService.init(transactionDto);
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, transactionDto.getTransactionType() + " successfully initiated.");

        return "redirect:" + Web.Endpoint.TRANSACTION_INIT;
    }

    @PostMapping(value = Web.Endpoint.TRANSACTION_APPROVE + "/{transactionId}")
    public String approveTransaction(@PathVariable Long transactionId, RedirectAttributes redirect) {

        transactionFacadeService.approve(transactionId);
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Transaction successfully approved!");

        return "redirect:" + Web.Endpoint.TRANSACTIONS + "/" + transactionId;
    }

    @PostMapping(value = Web.Endpoint.TRANSACTION_REJECT + "/{transactionId}")
    public String rejectTransaction(@PathVariable Long transactionId, @RequestParam String rejectReason, RedirectAttributes redirect) {

        if(rejectReason.isBlank())
            redirect.addFlashAttribute(Web.MessageTag.ERROR, "Reject reason is mandatory!");
        else {
            redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Transaction successfully rejected!");
            transactionFacadeService.reject(transactionId, rejectReason);
        }

        return "redirect:" + Web.Endpoint.TRANSACTIONS + "/" + transactionId;
    }

    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Transaction.MENU, null};
    }
}
