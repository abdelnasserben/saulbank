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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Stream;

@Controller
public class AccountController implements PageTitleConfig {

    private final AccountFacadeService accountFacadeService;
    private final CustomerFacadeService customerFacadeService;

    public AccountController(AccountFacadeService accountFacadeService, CustomerFacadeService customerFacadeService) {
        this.accountFacadeService = accountFacadeService;
        this.customerFacadeService = customerFacadeService;
    }

    @GetMapping(value = Web.Endpoint.ACCOUNT_ROOT)
    public String listingTrunks(Model model) {

        List<TrunkDto> customerAccounts = accountFacadeService.findAllTrunks().stream()
                .distinct()
                .peek(trunkDto -> StatedObjectFormatter.format(trunkDto.getAccount()))
                .toList();

        configPageTitle(model, Web.Menu.Account.ROOT);
        model.addAttribute("trunks", customerAccounts);
        return Web.View.ACCOUNT_LIST;
    }

    @GetMapping(value = Web.Endpoint.ACCOUNT_ROOT + "/{trunkId}")
    public String trunkDetails(Model model, @PathVariable Long trunkId) {

        TrunkDto trunkDto = Stream.of(accountFacadeService.findTrunkById(trunkId))
                .peek(t -> StatedObjectFormatter.format(t.getAccount()))
                .findFirst()
                .get();
        configPageTitle(model, "Account Details");
        model.addAttribute("trunk", trunkDto);
        return Web.View.ACCOUNT_DETAILS;
    }


    @PostMapping(value = Web.Endpoint.ACCOUNT_ACTIVATE + "/{trunkId}")
    public String activateTrunk(@PathVariable Long trunkId, RedirectAttributes redirect) {

        accountFacadeService.activateTrunk(trunkId);
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Account successfully activated !");

        return String.format("redirect:%s/%d", Web.Endpoint.ACCOUNT_ROOT , trunkId);
    }

    @PostMapping(value = Web.Endpoint.ACCOUNT_DEACTIVATE + "/{trunkId}")
    public String deactivateTrunk(@PathVariable Long trunkId, RedirectAttributes redirect) {

        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Account successfully deactivated!");
        accountFacadeService.deactivateTrunk(trunkId);

        return String.format("redirect:%s/%d", Web.Endpoint.ACCOUNT_ROOT , trunkId);
    }

    @GetMapping(value = Web.Endpoint.ACCOUNT_AFFILIATION)
    public String trunkAffiliationsDetails(Model model, @RequestParam(name = "code", required = false) String code) {

        if(code != null) {
            try {
                AccountDto accountDto = accountFacadeService.findTrunkByNumber(code).getAccount();
                List<TrunkDto> trunks = accountFacadeService.findAllTrunks(accountDto).stream()
                        .peek(t -> StatedObjectFormatter.format(t.getAccount()))
                        .peek(t -> StatedObjectFormatter.format(t.getCustomer()))
                        .toList();

                model.addAttribute("account", StatedObjectFormatter.format(accountDto));
                model.addAttribute("trunks", trunks);
            } catch (ResourceNotFoundException ex) {
                model.addAttribute(Web.MessageTag.ERROR, "Account not found");
            }
        }

        configPageTitle(model, Web.Menu.Account.AFFILIATION);
        return Web.View.ACCOUNT_AFFILIATION;
    }

    @GetMapping(value = Web.Endpoint.ACCOUNT_AFFILIATION + "/{accountNumber}")
    public String addNewAffiliateOnTrunk(Model model, @PathVariable String accountNumber, @RequestParam(name="member", required = false) String customerIdentity) {

        if(customerIdentity != null)
            model.addAttribute("customer", customerFacadeService.findByIdentity(customerIdentity));
        else model.addAttribute("customer",  new CustomerDto());

        TrunkDto trunkDto = Stream.of(accountFacadeService.findTrunkByNumber(accountNumber))
                .peek(t -> StatedObjectFormatter.format(t.getAccount()))
                .findFirst()
                .get();

        model.addAttribute("trunk",  trunkDto);
        configPageTitle(model, Web.Menu.Account.AFFILIATION_ADD);
        return Web.View.ACCOUNT_AFFILIATION_ADD;
    }

    @PostMapping(value = Web.Endpoint.ACCOUNT_AFFILIATION + "/{accountNumber}")
    public String addNewAffiliateOnTrunk(Model model, @Valid CustomerDto customerDto, BindingResult binding,
                                         @PathVariable String accountNumber,
                                         @RequestParam(name = "member") String customerIdentity,
                                         RedirectAttributes redirect) {

        CustomerDto nextAffiliate;
        if(customerIdentity.isEmpty()) {
            if(binding.hasErrors()) {
                redirect.addFlashAttribute(Web.MessageTag.ERROR, "Invalid Information");
                return String.format("redirect:%s/%s", Web.Endpoint.ACCOUNT_AFFILIATION, accountNumber);
            }

            nextAffiliate = customerDto;

        } else nextAffiliate = customerFacadeService.findByIdentity(customerIdentity);


        accountFacadeService.addAffiliate(nextAffiliate, accountNumber);
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Successful affiliation");

        return String.format("redirect:%s/%s", Web.Endpoint.ACCOUNT_AFFILIATION, accountNumber);
    }

    @PostMapping(value = Web.Endpoint.ACCOUNT_AFFILIATION + "/{trunkId}/" + "remove/" + "{customerIdentityNumber}")
    public String manageTrunkAffiliation(@PathVariable Long trunkId, @PathVariable String customerIdentityNumber, RedirectAttributes redirect) {

        TrunkDto trunkDto = accountFacadeService.findTrunkById(trunkId);
        CustomerDto customerDto = customerFacadeService.findByIdentity(customerIdentityNumber);

        accountFacadeService.removeAffiliate(customerDto, trunkDto.getAccount().getAccountNumber());
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Affiliate removed successfully");

        return String.format("redirect:%s?code=%s", Web.Endpoint.ACCOUNT_AFFILIATION, trunkDto.getAccount().getAccountNumber());
    }


    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Account.MENU, null};
    }
}