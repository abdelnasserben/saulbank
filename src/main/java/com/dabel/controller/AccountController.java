package com.dabel.controller;

import com.dabel.app.StatedObjectFormatter;
import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.Web;
import com.dabel.dto.TrunkDto;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.branch.BranchFacadeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Stream;

@Controller
public class AccountController implements PageTitleConfig {

    private final BranchFacadeService branchFacadeService;
    private final AccountFacadeService accountFacadeService;

    public AccountController(BranchFacadeService branchFacadeService, AccountFacadeService accountFacadeService) {
        this.branchFacadeService = branchFacadeService;
        this.accountFacadeService = accountFacadeService;
    }

    @GetMapping(value = Web.Endpoint.ACCOUNT_ROOT)
    public String listingTrunks(Model model) {

        List<TrunkDto> customerAccounts = accountFacadeService.findCustomerTrunks().stream()
                .peek(trunkDto -> StatedObjectFormatter.format(trunkDto.getAccount()))
                .toList();

        configPageTitle(model, Web.Menu.Bank.Branches.ROOT);
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


    @GetMapping(value = Web.Endpoint.ACCOUNT_ACTIVATE + "/{trunkId}")
    public String activateTrunk(@PathVariable Long trunkId, RedirectAttributes redirect) {

        accountFacadeService.activateTrunk(trunkId);
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Account successfully activated !");

        return String.format("redirect:%s/%d", Web.Endpoint.CARD_ROOT , trunkId);
    }

    @PostMapping(value = Web.Endpoint.ACCOUNT_DEACTIVATE + "/{trunkId}")
    public String deactivateTrunk(@PathVariable Long trunkId, RedirectAttributes redirect) {

        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Account successfully deactivated!");
        accountFacadeService.deactivateTrunk(trunkId);

        return String.format("redirect:%s/%d", Web.Endpoint.CARD_ROOT , trunkId);
    }


    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Account.MENU, null};
    }
}