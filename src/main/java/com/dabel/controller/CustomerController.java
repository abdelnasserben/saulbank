package com.dabel.controller;

import com.dabel.app.Helper;
import com.dabel.app.StatedObjectFormatter;
import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.*;
import com.dabel.dto.*;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.card.CardFacadeService;
import com.dabel.service.customer.CustomerFacadeService;
import com.dabel.service.exchange.ExchangeFacadeService;
import com.dabel.service.loan.LoanFacadeService;
import com.dabel.service.storage.ProfileFileStorageService;
import com.dabel.service.storage.SignatureFileStorageService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collection;
import java.util.List;

@Controller
public class CustomerController implements PageTitleConfig {

    private static final String INVALID_INFORMATION_ERROR_MESSAGE = "Invalid information!";
    private static final String CUSTOMER_ADDED_SUCCESS_MESSAGE = "Customer added successfully!";
    private static final String CUSTOMER_UPDATED_SUCCESS_MESSAGE = "Customer information updated successfully!";

    private final CustomerFacadeService customerFacadeService;
    private final AccountFacadeService accountFacadeService;
    private final TransactionFacadeService transactionFacadeService;
    private final ExchangeFacadeService exchangeFacadeService;
    private final LoanFacadeService loanFacadeService;
    private final CardFacadeService cardFacadeService;
    private final UserService userService;

    @Autowired
    public CustomerController(CustomerFacadeService customerFacadeService, AccountFacadeService accountFacadeService, TransactionFacadeService transactionFacadeService, ExchangeFacadeService exchangeFacadeService, LoanFacadeService loanFacadeService, CardFacadeService cardFacadeService, UserService userService) {
        this.customerFacadeService = customerFacadeService;
        this.accountFacadeService = accountFacadeService;
        this.transactionFacadeService = transactionFacadeService;
        this.exchangeFacadeService = exchangeFacadeService;
        this.loanFacadeService = loanFacadeService;
        this.cardFacadeService = cardFacadeService;
        this.userService = userService;
    }

    @GetMapping(value = Web.Endpoint.CUSTOMERS)
    public String listCustomers(Model model) {

        configPageTitle(model, Web.Menu.Customer.ROOT);
        model.addAttribute("customers", StatedObjectFormatter.format(customerFacadeService.getAll()));
        return Web.View.CUSTOMERS;
    }

    @GetMapping(value = Web.Endpoint.CUSTOMER_ADD)
    public String showCustomerAddPage(Model model, CustomerDto customerDto) {
        configPageTitle(model, Web.Menu.Customer.ADD);
        return Web.View.CUSTOMER_ADD;
    }

    @PostMapping(value = Web.Endpoint.CUSTOMER_ADD)
    public String processAddCustomer(Model model, @Valid CustomerDto customerDto,
                                     @RequestParam String accountName,
                                     @RequestParam AccountType accountType,
                                     @RequestParam AccountProfile accountProfile,
                                     @RequestParam MultipartFile avatar,
                                     @RequestParam MultipartFile signature,
                                     BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if(bindingResult.hasErrors()) {
            configPageTitle(model, Web.Menu.Customer.ADD);
            model.addAttribute(Web.MessageTag.ERROR, INVALID_INFORMATION_ERROR_MESSAGE);
            return Web.View.CUSTOMER_ADD;
        }

        customerDto.setBranch(userService.getAuthenticated().getBranch());

        //TODO: save customer pictures
        String savedAvatarName = new ProfileFileStorageService().store(avatar, customerDto.getIdentityNumber());
        String savedSignatureName = new SignatureFileStorageService().store(signature, customerDto.getIdentityNumber());
        customerDto.setProfilePicture(savedAvatarName);
        customerDto.setSignaturePicture(savedSignatureName);

        customerFacadeService.createNewCustomerWithAccount(customerDto, accountName, accountType, accountProfile);

        redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, CUSTOMER_ADDED_SUCCESS_MESSAGE);
        return "redirect:" + Web.Endpoint.CUSTOMER_ADD;
    }

    @GetMapping(value = Web.Endpoint.CUSTOMERS + "/{customerId}")
    public String showCustomerDetails(@PathVariable Long customerId, Model model) {

        CustomerDto customerDto = customerFacadeService.getById(customerId);

        List<TrunkDto> customerAccounts = accountFacadeService.getAllTrunksByCustomer(customerDto).stream()
                .peek(trunkDto -> StatedObjectFormatter.format(trunkDto.getAccount()))
                .toList();
        double totalBalance = customerAccounts.stream()
                .map(TrunkDto::getAccount)
                .mapToDouble(AccountDto::getBalance)
                .sum();

        List<TransactionDto> lastTenCustomerTransactions = customerAccounts.stream()
                .map(trunkDto -> transactionFacadeService.getAccountTransactions(trunkDto.getAccount()))
                .flatMap(Collection::stream)
                .filter(transactionDto -> !transactionDto.getTransactionType().equals(TransactionType.FEE.name()))
                .limit(10)
                .toList();

        List<ExchangeDto> lastTenCustomerExchanges = exchangeFacadeService.getAllByCustomerIdentityNumber(customerDto.getIdentityNumber()).stream()
                .limit(10)
                .toList();

        List<LoanDto> customerLoans = loanFacadeService.getCustomerLoansByHisIdentityNumber(customerDto.getIdentityNumber()).stream()
                .toList();

        double totalLoan = customerLoans.stream()
                .filter(l -> l.getStatus().equals(Status.ACTIVE.code()))
                .mapToDouble(l -> l.getAccount().getBalance())
                .sum();

        List<CardDto> customerCards = customerAccounts.stream()
                .map(trunkDto -> cardFacadeService.getAllCardsByCustomer(trunkDto.getCustomer()))
                .flatMap(Collection::stream)
                .peek(c -> c.setCardNumber(Helper.hideCardNumber(c.getCardNumber())))
                .toList();

        boolean notifyNoActiveCreditCards = customerCards.stream()
                .anyMatch(c -> c.getStatus().equals(Status.ACTIVE.code()));


        configPageTitle(model, "Customer Details");
        model.addAttribute("customer", StatedObjectFormatter.format(customerDto));
        model.addAttribute("trunks", customerAccounts);
        model.addAttribute("totalBalance", Helper.formatCurrency(totalBalance));
        model.addAttribute("cards", StatedObjectFormatter.format(customerCards));
        model.addAttribute("notifyNoActiveCreditCards", notifyNoActiveCreditCards);
        model.addAttribute("transactions", StatedObjectFormatter.format(lastTenCustomerTransactions));
        model.addAttribute("completionRate", customerFacadeService.getCompletionRate(customerDto));
        model.addAttribute("exchanges", StatedObjectFormatter.format(lastTenCustomerExchanges));
        model.addAttribute("loans", StatedObjectFormatter.format(customerLoans));
        model.addAttribute("totalLoan", Helper.formatCurrency(totalLoan));

        return Web.View.CUSTOMER_DETAILS;
    }

    @PostMapping(value = Web.Endpoint.CUSTOMERS + "/{customerId}")
    public String updateCustomerGeneralInfo(@Valid CustomerDto customerDto, BindingResult binding, RedirectAttributes redirect) {

        if(binding.hasErrors())
            redirect.addFlashAttribute(Web.MessageTag.ERROR, INVALID_INFORMATION_ERROR_MESSAGE);
        else {
            redirect.addFlashAttribute(Web.MessageTag.SUCCESS, CUSTOMER_UPDATED_SUCCESS_MESSAGE);
            customerFacadeService.updateCustomerDetails(customerDto);
        }

        return String.format("redirect:%s/%d", Web.Endpoint.CUSTOMERS, customerDto.getCustomerId());
    }

    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Customer.MENU, null};
    }
}