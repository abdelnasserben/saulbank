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

import java.util.List;


@Controller
public class TransactionController implements PageTitleConfig {

    private static final String INVALID_INFORMATION_ERROR_MESSAGE = "Please provide valid transaction information!";
    private static final String REJECT_REASON_ERROR_MESSAGE = "A rejection reason is required!";
    private static final String TRANSACTION_INITIATED_SUCCESS_MESSAGE = "Transaction initiated successfully.";
    private static final String TRANSACTION_APPROVED_SUCCESS_MESSAGE = "Transaction approved successfully!";
    private static final String TRANSACTION_REJECTED_SUCCESS_MESSAGE = "Transaction rejected successfully!";


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
    public String listTransactions(Model model) {

        configPageTitle(model, Web.Menu.Transaction.ROOT);
        List<TransactionDto> transactions = transactionFacadeService.getAll().stream()
                .filter(transaction -> !TransactionType.FEE.name().equals(transaction.getTransactionType()))
                .toList();

        model.addAttribute("transactions", StatedObjectFormatter.format(transactions));
        return Web.View.TRANSACTIONS;
    }

    @GetMapping(value = Web.Endpoint.TRANSACTIONS + "/{transactionId}")
    public String showTransactionDetails(@PathVariable Long transactionId, Model model) {

        TransactionDto transactionDto = transactionFacadeService.getById(transactionId);

        model.addAttribute("transaction", StatedObjectFormatter.format(transactionDto));
        configPageTitle(model, "Transaction Details");
        return Web.View.TRANSACTION_DETAILS;
    }

    @GetMapping(value = Web.Endpoint.TRANSACTION_INIT)
    public String initializeTransaction(Model model, TransactionDto transactionDto) {
        configPageTitle(model, Web.Menu.Transaction.INIT);

        return Web.View.TRANSACTION_INIT;
    }

    @PostMapping(value = Web.Endpoint.TRANSACTION_INIT)
    public String handleTransactionInitialization(Model model, @Valid TransactionDto transactionDto, BindingResult bindingResult,
                                                  @RequestParam String initiatorAccountNumber,
                                                  @RequestParam(name = "receiverAccountNumber", required = false) String receiverAccountNumber,
                                                  RedirectAttributes redirectAttributes) {

        if(bindingResult.hasErrors() || isInvalidTransfer(transactionDto, receiverAccountNumber)) {
            configPageTitle(model, Web.Menu.Transaction.INIT);
            model.addAttribute(Web.MessageTag.ERROR, INVALID_INFORMATION_ERROR_MESSAGE);

            return Web.View.TRANSACTION_INIT;
        }

        setupTransactionAccounts(transactionDto, initiatorAccountNumber, receiverAccountNumber);
        setTransactionBranchAndSource(transactionDto);

        transactionFacadeService.init(transactionDto);
        redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, TRANSACTION_INITIATED_SUCCESS_MESSAGE);

        return "redirect:" + Web.Endpoint.TRANSACTION_INIT;
    }

    @PostMapping(value = Web.Endpoint.TRANSACTION_APPROVE + "/{transactionId}")
    public String approveTransaction(@PathVariable Long transactionId, RedirectAttributes redirect) {

        transactionFacadeService.approve(transactionId);
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, TRANSACTION_APPROVED_SUCCESS_MESSAGE);

        return redirectToTransactionDetails(transactionId);
    }

    @PostMapping(value = Web.Endpoint.TRANSACTION_REJECT + "/{transactionId}")
    public String rejectTransaction(@PathVariable Long transactionId, @RequestParam String rejectReason, RedirectAttributes redirectAttributes) {

        if(rejectReason.isBlank())
            redirectAttributes.addFlashAttribute(Web.MessageTag.ERROR, REJECT_REASON_ERROR_MESSAGE);
        else {
            redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, TRANSACTION_REJECTED_SUCCESS_MESSAGE);
            transactionFacadeService.reject(transactionId, rejectReason);
        }

        return redirectToTransactionDetails(transactionId);
    }

    private boolean isInvalidTransfer(TransactionDto transactionDto, String receiverAccountNumber) {
        return TransactionType.TRANSFER.name().equalsIgnoreCase(transactionDto.getTransactionType()) && receiverAccountNumber.isBlank();
    }

    private void setupTransactionAccounts(TransactionDto transactionDto, String initiatorAccountNumber, String receiverAccountNumber) {
        AccountDto initiatorAccount = accountFacadeService.getTrunkByNumber(initiatorAccountNumber).getAccount();
        transactionDto.setInitiatorAccount(initiatorAccount);

        if (TransactionType.TRANSFER.name().equalsIgnoreCase(transactionDto.getTransactionType())) {
            AccountDto receiverAccount = accountFacadeService.getTrunkByNumber(receiverAccountNumber).getAccount();
            transactionDto.setReceiverAccount(receiverAccount);
        }
    }

    private void setTransactionBranchAndSource(TransactionDto transactionDto) {
        BranchDto branchDto = userService.getAuthenticated().getBranch();
        transactionDto.setBranch(branchDto);
        transactionDto.setSourceType(SourceType.ONLINE.name());
        transactionDto.setSourceValue(branchDto.getBranchName());
    }

    private static String redirectToTransactionDetails(Long transactionId) {
        return "redirect:" + Web.Endpoint.TRANSACTIONS + "/" + transactionId;
    }

    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Transaction.MENU, null};
    }
}
