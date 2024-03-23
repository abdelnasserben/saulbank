package com.dabel.controller;

import com.dabel.app.StatedObjectFormatter;
import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.Status;
import com.dabel.constant.TransactionType;
import com.dabel.constant.Web;
import com.dabel.dto.*;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.branch.BranchFacadeService;
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

    public CustomerController(CustomerFacadeService customerFacadeService, BranchFacadeService branchFacadeService, AccountFacadeService accountFacadeService, TransactionFacadeService transactionFacadeService, ExchangeFacadeService exchangeFacadeService, LoanFacadeService loanFacadeService) {
        this.customerFacadeService = customerFacadeService;
        this.branchFacadeService = branchFacadeService;
        this.accountFacadeService = accountFacadeService;
        this.transactionFacadeService = transactionFacadeService;
        this.exchangeFacadeService = exchangeFacadeService;
        this.loanFacadeService = loanFacadeService;
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
                                 @RequestParam(defaultValue = "Saving") String accountType,
                                 BindingResult binding, RedirectAttributes redirect) {

        if(binding.hasErrors()) {
            configPageTitle(model, Web.Menu.Customer.ADD);
            model.addAttribute(Web.MessageTag.ERROR, "Invalid information !");
            return "customers-add";
        }

        //TODO: set branch - We'll replace this automatically by user authenticated
        BranchDto branchDto = branchFacadeService.findById(1L);
        customerDto.setBranch(branchDto);

        customerFacadeService.create(customerDto, accountType);

        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Customer added successfully !");
        return "redirect:" + Web.Endpoint.CUSTOMER_ADD;
    }

    @GetMapping(value = Web.Endpoint.CUSTOMER_ROOT + "/{customerId}")
    public String customerDetails(@PathVariable Long customerId, Model model) {

        CustomerDto customerDto = customerFacadeService.findById(customerId);

        List<TrunkDto> customerAccounts = accountFacadeService.findAllCustomerAccounts(customerDto).stream()
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
                .filter(l -> l.getStatus().equals(Status.ACTIVE.code()))
                .toList();

        double totalLoan = customerLoans.stream()
                .mapToDouble(LoanDto::getTotalAmount)
                .sum();

//        List<CardDTO> customerCards = cardFacadeService.findAllByCustomerId(customerId)
//                .stream()
//                .peek(c -> c.setCardNumber(CardNumberFormatter.hide(c.getCardNumber())))
//                .toList();
//        boolean notifyNoActiveCreditCards = customerCards.stream()
//                .anyMatch(c -> c.getStatus().equals(Status.ACTIVE.code()));
//
//
//        List<PaymentDTO> lastTenCustomerPayments = paymentFacadeService.findAllByCustomerId(customerId).stream()
//                .limit(10)
//                .toList();
//


        configPageTitle(model, "Customer Details");
        model.addAttribute("customer", StatedObjectFormatter.format(customerDto));
        model.addAttribute("trunks", customerAccounts);
        model.addAttribute("totalBalance", totalBalance);
//        model.addAttribute("cards", StatedObjectFormatter.format(customerCards));
//        model.addAttribute("notifyNoActiveCreditCards", notifyNoActiveCreditCards);
        model.addAttribute("transactions", StatedObjectFormatter.format(lastTenCustomerTransactions));
//        model.addAttribute("payments", StatedObjectFormatter.format(lastTenCustomerPayments));
        model.addAttribute("exchanges", StatedObjectFormatter.format(lastTenCustomerExchanges));
        model.addAttribute("loans", StatedObjectFormatter.format(customerLoans));
        model.addAttribute("totalLoan", totalLoan);

        return Web.View.CUSTOMER_DETAILS;
    }

    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Customer.MENU, null};
    }
}