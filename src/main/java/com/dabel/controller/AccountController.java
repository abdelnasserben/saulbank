package com.dabel.controller;

import com.dabel.app.StatedObjectFormatter;
import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.Web;
import com.dabel.dto.AccountDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.TrunkDto;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.customer.CustomerFacadeService;
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
public class AccountController implements PageTitleConfig {

    private static final String MESSAGE_ACCOUNT_ACTIVATED = "Account successfully activated!";
    private static final String MESSAGE_ACCOUNT_DEACTIVATED = "Account successfully deactivated!";
    private static final String MESSAGE_AFFILIATION_SUCCESS = "Successful affiliation!";
    private static final String MESSAGE_AFFILIATE_REMOVED = "Affiliate removed successfully!";
    private static final String ERROR_ACCOUNT_NOT_FOUND = "Account not found";
    private static final String ERROR_INVALID_INFORMATION = "Invalid Information!";

    private final AccountFacadeService accountFacadeService;
    private final CustomerFacadeService customerFacadeService;

    @Autowired
    public AccountController(AccountFacadeService accountFacadeService, CustomerFacadeService customerFacadeService) {
        this.accountFacadeService = accountFacadeService;
        this.customerFacadeService = customerFacadeService;
    }

    @GetMapping(value = Web.Endpoint.ACCOUNTS)
    public String listAllAccounts(Model model) {

        List<TrunkDto> customerAccounts = accountFacadeService.getAllTrunks().stream()
                .peek(trunkDto -> StatedObjectFormatter.format(trunkDto.getAccount()))
                .toList();

        configPageTitle(model, Web.Menu.Account.ROOT);
        model.addAttribute("trunks", customerAccounts);
        return Web.View.ACCOUNTS;
    }

    @GetMapping(value = Web.Endpoint.ACCOUNTS + "/{trunkId}")
    public String showAccountDetails(Model model, @PathVariable Long trunkId) {
        TrunkDto trunk = accountFacadeService.getTrunkById(trunkId);
        StatedObjectFormatter.format(trunk.getAccount());

        configPageTitle(model, "Account Details");
        model.addAttribute("trunk", trunk);
        return Web.View.ACCOUNT_DETAILS;
    }


    @PostMapping(value = Web.Endpoint.ACCOUNT_ACTIVATE + "/{trunkId}")
    public String activateAccount(@PathVariable Long trunkId, RedirectAttributes redirectAttributes) {

        accountFacadeService.activateTrunkById(trunkId);
        redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, MESSAGE_ACCOUNT_ACTIVATED);

        return buildRedirectUrl(Web.Endpoint.ACCOUNTS, trunkId);
    }

    @PostMapping(value = Web.Endpoint.ACCOUNT_DEACTIVATE + "/{trunkId}")
    public String deactivateAccount(@PathVariable Long trunkId, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS,  MESSAGE_ACCOUNT_DEACTIVATED);
        accountFacadeService.deactivateTrunkById(trunkId);

        return buildRedirectUrl(Web.Endpoint.ACCOUNTS, trunkId);
    }

    @GetMapping(value = Web.Endpoint.ACCOUNT_AFFILIATION)
    public String showAccountAffiliations(Model model, @RequestParam(name = "code", required = false) String accountNumber) {

        if(accountNumber != null) {
            try {
                AccountDto account = accountFacadeService.getTrunkByNumber(accountNumber).getAccount();
                List<TrunkDto> affiliatedAccounts = accountFacadeService.getAllTrunksByAccount(account).stream()
                        .peek(t -> {
                            StatedObjectFormatter.format(t.getAccount());
                            StatedObjectFormatter.format(t.getCustomer());
                        })
                        .toList();

                model.addAttribute("account", StatedObjectFormatter.format(account));
                model.addAttribute("trunks", affiliatedAccounts);
            } catch (ResourceNotFoundException ex) {
                model.addAttribute(Web.MessageTag.ERROR, ERROR_ACCOUNT_NOT_FOUND);
            }
        }

        configPageTitle(model, Web.Menu.Account.AFFILIATION);
        return Web.View.ACCOUNT_AFFILIATION;
    }

    @GetMapping(value = Web.Endpoint.ACCOUNT_AFFILIATION + "/{accountNumber}")
    public String showAffiliationManagementPage(Model model, @PathVariable String accountNumber, @RequestParam(name="member", required = false) String customerIdentity) {
        TrunkDto trunk = accountFacadeService.getTrunkByNumber(accountNumber);
        StatedObjectFormatter.format(trunk.getAccount());

        CustomerDto customer = (customerIdentity != null) ? customerFacadeService.getByIdentityNumber(customerIdentity) : new CustomerDto();

        model.addAttribute("customer", customer);
        model.addAttribute("trunk",  trunk);
        configPageTitle(model, Web.Menu.Account.AFFILIATION_ADD);
        return Web.View.ACCOUNT_AFFILIATION_ADD;
    }

    @PostMapping(value = Web.Endpoint.ACCOUNT_AFFILIATION + "/{accountNumber}")
    public String addAffiliate(Model model, @Valid CustomerDto customerDto, BindingResult bindingResult,
                               @PathVariable String accountNumber,
                               @RequestParam(name = "member") String customerIdentity,
                               RedirectAttributes redirectAttributes) {

        CustomerDto affiliate = customerIdentity.isEmpty() ? validateCustomerData(customerDto, bindingResult, redirectAttributes) : customerFacadeService.getByIdentityNumber(customerIdentity);

        if (affiliate == null)
            return buildRedirectUrl(Web.Endpoint.ACCOUNT_AFFILIATION, accountNumber);

        accountFacadeService.addAffiliateToAccount(affiliate, accountNumber);
        redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, MESSAGE_AFFILIATION_SUCCESS);

        return  buildRedirectUrl(Web.Endpoint.ACCOUNT_AFFILIATION, accountNumber);
    }

    @PostMapping(value = Web.Endpoint.ACCOUNT_AFFILIATION + "/{trunkId}/" + "remove/" + "{customerIdentityNumber}")
    public String removeAffiliate(@PathVariable Long trunkId, @PathVariable String customerIdentityNumber, RedirectAttributes redirectAttributes) {

        TrunkDto trunk = accountFacadeService.getTrunkById(trunkId);
        CustomerDto customer = customerFacadeService.getByIdentityNumber(customerIdentityNumber);

        accountFacadeService.removeAffiliateFromAccount(customer, trunk.getAccount().getAccountNumber());
        redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, MESSAGE_AFFILIATE_REMOVED);

        return String.format("redirect:%s?code=%s", Web.Endpoint.ACCOUNT_AFFILIATION, trunk.getAccount().getAccountNumber());
    }

    private String buildRedirectUrl(String endpoint, Object identifier) {
        return String.format("redirect:%s/%s", endpoint, identifier);
    }

    private CustomerDto validateCustomerData(CustomerDto customerDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(Web.MessageTag.ERROR, ERROR_INVALID_INFORMATION);
            return null;
        }
        return customerDto;
    }

    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Account.MENU, null};
    }
}