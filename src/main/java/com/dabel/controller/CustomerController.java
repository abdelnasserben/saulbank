package com.dabel.controller;

import com.dabel.app.Helper;
import com.dabel.app.StatedObjectFormatter;
import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.*;
import com.dabel.dto.*;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.branch.BranchFacadeService;
import com.dabel.service.card.CardFacadeService;
import com.dabel.service.customer.CustomerFacadeService;
import com.dabel.service.exchange.ExchangeFacadeService;
import com.dabel.service.loan.LoanFacadeService;
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

import java.util.Collection;
import java.util.List;

@Controller
public class CustomerController implements PageTitleConfig {

    private final CustomerFacadeService customerFacadeService;
    private final BranchFacadeService branchFacadeService;
    private final AccountFacadeService accountFacadeService;
    private final TransactionFacadeService transactionFacadeService;
    private final ExchangeFacadeService exchangeFacadeService;
    private final LoanFacadeService loanFacadeService;
    private final CardFacadeService cardFacadeService;

    public CustomerController(CustomerFacadeService customerFacadeService, BranchFacadeService branchFacadeService, AccountFacadeService accountFacadeService, TransactionFacadeService transactionFacadeService, ExchangeFacadeService exchangeFacadeService, LoanFacadeService loanFacadeService, CardFacadeService cardFacadeService) {
        this.customerFacadeService = customerFacadeService;
        this.branchFacadeService = branchFacadeService;
        this.accountFacadeService = accountFacadeService;
        this.transactionFacadeService = transactionFacadeService;
        this.exchangeFacadeService = exchangeFacadeService;
        this.loanFacadeService = loanFacadeService;
        this.cardFacadeService = cardFacadeService;
    }

    @GetMapping(value = Web.Endpoint.CUSTOMER_ROOT)
    public String listingCustomers(Model model) {

        configPageTitle(model, Web.Menu.Customer.ROOT);
        model.addAttribute("customers", StatedObjectFormatter.format(customerFacadeService.findAll()));
        return Web.View.CUSTOMER_LIST;
    }

    @GetMapping(value = Web.Endpoint.CUSTOMER_ADD)
    public String addNewCustomer(Model model, CustomerDto customerDto) {
        configPageTitle(model, Web.Menu.Customer.ADD);
        return Web.View.CUSTOMER_ADD;
    }

    @PostMapping(value = Web.Endpoint.CUSTOMER_ADD)
    public String addNewCustomer(Model model, @Valid CustomerDto customerDto,
                                 @RequestParam String accountName,
                                 @RequestParam AccountType accountType,
                                 @RequestParam AccountProfile accountProfile,
                                 BindingResult binding, RedirectAttributes redirect) {

        if(binding.hasErrors()) {
            configPageTitle(model, Web.Menu.Customer.ADD);
            model.addAttribute(Web.MessageTag.ERROR, "Invalid information !");
            return "customers-add";
        }

        //TODO: set branch - We'll replace this automatically by user authenticated
        BranchDto branchDto = branchFacadeService.findById(1L);
        customerDto.setBranch(branchDto);

        customerFacadeService.create(customerDto, accountName, accountType, accountProfile);

        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Customer added successfully !");
        return "redirect:" + Web.Endpoint.CUSTOMER_ADD;
    }

    @GetMapping(value = Web.Endpoint.CUSTOMER_ROOT + "/{customerId}")
    public String customerDetails(@PathVariable Long customerId, Model model) {

        CustomerDto customerDto = customerFacadeService.findById(customerId);

        List<TrunkDto> customerAccounts = accountFacadeService.findAllTrunks(customerDto).stream()
                .peek(trunkDto -> StatedObjectFormatter.format(trunkDto.getAccount()))
                .toList();
        double totalBalance = customerAccounts.stream()
                .map(TrunkDto::getAccount)
                .mapToDouble(AccountDto::getBalance)
                .sum();

        List<TransactionDto> lastTenCustomerTransactions = customerAccounts.stream()
                .map(trunkDto -> transactionFacadeService.findAllByAccount(trunkDto.getAccount()))
                .flatMap(Collection::stream)
                .filter(transactionDto -> !transactionDto.getTransactionType().equals(TransactionType.FEE.name()))
                .limit(10)
                .toList();

        List<ExchangeDto> lastTenCustomerExchanges = exchangeFacadeService.findAllByCustomerIdentity(customerDto.getIdentityNumber()).stream()
                .limit(10)
                .toList();

        List<LoanDto> customerLoans = loanFacadeService.findAllByCustomerIdentityNumber(customerDto.getIdentityNumber()).stream()
                .toList();

        double totalLoan = customerLoans.stream()
                .filter(l -> l.getStatus().equals(Status.ACTIVE.code()))
                .mapToDouble(LoanDto::getTotalAmount)
                .sum();

        List<CardDto> customerCards = customerAccounts.stream()
                .map(trunkDto -> cardFacadeService.findAllAccountCards(trunkDto.getAccount()))
                .flatMap(Collection::stream)
                .peek(c -> c.setCardNumber(Helper.hideCardNumber(c.getCardNumber())))
                .toList();

        boolean notifyNoActiveCreditCards = customerCards.stream()
                .anyMatch(c -> c.getStatus().equals(Status.ACTIVE.code()));


        configPageTitle(model, "Customer Details");
        model.addAttribute("customer", StatedObjectFormatter.format(customerDto));
        model.addAttribute("trunks", customerAccounts);
        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("cards", StatedObjectFormatter.format(customerCards));
        model.addAttribute("notifyNoActiveCreditCards", notifyNoActiveCreditCards);
        model.addAttribute("transactions", StatedObjectFormatter.format(lastTenCustomerTransactions));
//        model.addAttribute("payments", StatedObjectFormatter.format(lastTenCustomerPayments));
        model.addAttribute("exchanges", StatedObjectFormatter.format(lastTenCustomerExchanges));
        model.addAttribute("loans", StatedObjectFormatter.format(customerLoans));
        model.addAttribute("totalLoan", totalLoan);

        return Web.View.CUSTOMER_DETAILS;
    }

    @PostMapping(value = Web.Endpoint.CUSTOMER_ROOT+ "/{customerId}")
    public String updateCustomerGeneralInfo(@Valid CustomerDto customerDto, BindingResult binding, RedirectAttributes redirect) {

        if(binding.hasErrors())
            redirect.addFlashAttribute(Web.MessageTag.ERROR, "Invalid information !");
        else {
            redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Customer information updated successfully !");
            customerFacadeService.update(customerDto);
        }

        return String.format("redirect:%s/%d", Web.Endpoint.CUSTOMER_ROOT, customerDto.getCustomerId());
    }

    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Customer.MENU, null};
    }
}