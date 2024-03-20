package com.dabel.controller;

import com.dabel.app.StatedObjectFormatter;
import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.App;
import com.dabel.dto.AccountDto;
import com.dabel.dto.BranchDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.TrunkDto;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.branch.BranchFacadeService;
import com.dabel.service.customer.CustomerFacadeService;
import jakarta.validation.Valid;
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
public class CustomerController implements PageTitleConfig {

    private final CustomerFacadeService customerFacadeService;
    private final BranchFacadeService branchFacadeService;
    private final AccountFacadeService accountFacadeService;

    public CustomerController(CustomerFacadeService customerFacadeService, BranchFacadeService branchFacadeService, AccountFacadeService accountFacadeService) {
        this.customerFacadeService = customerFacadeService;
        this.branchFacadeService = branchFacadeService;
        this.accountFacadeService = accountFacadeService;
    }

    @GetMapping(value = App.Endpoint.CUSTOMER_ROOT)
    public String listingCustomers(Model model) {

        configPageTitle(model, App.Menu.Customer.ROOT);
        model.addAttribute("customers", StatedObjectFormatter.format(customerFacadeService.findAll()));
        return App.View.CUSTOMER_LIST;
    }

    @GetMapping(value = App.Endpoint.CUSTOMER_ADD)
    public String addNewCustomer(Model model, CustomerDto customerDto) {
        configPageTitle(model, App.Menu.Customer.ADD);
        return App.View.CUSTOMER_ADD;
    }

    @PostMapping(value = App.Endpoint.CUSTOMER_ADD)
    public String addNewCustomer(Model model, @Valid CustomerDto customerDto,
                                 @RequestParam(defaultValue = "Saving") String accountType,
                                 BindingResult binding, RedirectAttributes redirect) {

        if(binding.hasErrors()) {
            configPageTitle(model, App.Menu.Customer.ADD);
            model.addAttribute(App.MessageTag.ERROR, "Invalid information !");
            return "customers-add";
        }

        //TODO: set branch - We'll replace this automatically by user authenticated
        BranchDto branchDto = branchFacadeService.findById(1L);
        customerDto.setBranch(branchDto);

        customerFacadeService.create(customerDto, accountType);

        redirect.addFlashAttribute(App.MessageTag.SUCCESS, "Customer added successfully !");
        return "redirect:" + App.Endpoint.CUSTOMER_ADD;
    }

    @GetMapping(value = App.Endpoint.CUSTOMER_ROOT + "/{customerId}")
    public String customerDetails(@PathVariable Long customerId, Model model) {

        CustomerDto customerDto = customerFacadeService.findById(customerId);

        List<TrunkDto> customerAccounts = accountFacadeService.findAllCustomerAccounts(customerDto).stream()
                .peek(trunkDto -> StatedObjectFormatter.format(trunkDto.getAccount()))
                .toList();
        double totalBalance = customerAccounts.stream()
                .map(TrunkDto::getAccount)
                .mapToDouble(AccountDto::getBalance)
                .sum();

//        List<CardDTO> customerCards = cardFacadeService.findAllByCustomerId(customerId)
//                .stream()
//                .peek(c -> c.setCardNumber(CardNumberFormatter.hide(c.getCardNumber())))
//                .toList();
//        boolean notifyNoActiveCreditCards = customerCards.stream()
//                .anyMatch(c -> c.getStatus().equals(Status.ACTIVE.code()));
//
//        List<TransactionDTO> lastTenCustomerTransactions = transactionFacadeService.findAllByCustomerId(customerId).stream()
//                .limit(10)
//                .toList();
//
//        List<PaymentDTO> lastTenCustomerPayments = paymentFacadeService.findAllByCustomerId(customerId).stream()
//                .limit(10)
//                .toList();
//
//        List<ExchangeDTO> lastTenCustomerExchanges = exchangeFacadeService.findAllByCustomerIdentity(customerDto.getIdentityNumber()).stream()
//                .limit(10)
//                .toList();
//
//        List<LoanDTO> customerLoans = loanFacadeService.findAllByCustomerIdentityNumber(customerDto.getIdentityNumber()).stream()
//                .filter(l -> l.getStatus().equals(Status.ACTIVE.code()))
//                .toList();
//
//        double totalLoan = customerLoans.stream()
//                .mapToDouble(LoanDTO::getTotalAmount)
//                .sum();

        configPageTitle(model, "Customer Details");
        model.addAttribute("customer", StatedObjectFormatter.format(customerDto));
        model.addAttribute("trunks", customerAccounts);
        model.addAttribute("totalBalance", totalBalance);
//        model.addAttribute("cards", StatedObjectFormatter.format(customerCards));
//        model.addAttribute("notifyNoActiveCreditCards", notifyNoActiveCreditCards);
//        model.addAttribute("transactions", StatedObjectFormatter.format(lastTenCustomerTransactions));
//        model.addAttribute("payments", StatedObjectFormatter.format(lastTenCustomerPayments));
//        model.addAttribute("exchanges", StatedObjectFormatter.format(lastTenCustomerExchanges));
//        model.addAttribute("loans", StatedObjectFormatter.format(customerLoans));
//        model.addAttribute("totalLoan", totalLoan);

        return App.View.CUSTOMER_DETAILS;
    }

    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{App.Menu.Customer.MENU, null};
    }
}