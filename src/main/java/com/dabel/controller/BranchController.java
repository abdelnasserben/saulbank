package com.dabel.controller;

import com.dabel.app.StatedObjectFormatter;
import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.Web;
import com.dabel.dto.AccountDto;
import com.dabel.dto.BranchDto;
import com.dabel.dto.LedgerDto;
import com.dabel.service.account.AccountOperationService;
import com.dabel.service.branch.BranchFacadeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

@Controller
public class BranchController implements PageTitleConfig {

    private final BranchFacadeService branchFacadeService;
    private final AccountOperationService accountOperationService;

    public BranchController(BranchFacadeService branchFacadeService, AccountOperationService accountOperationService) {
        this.branchFacadeService = branchFacadeService;
        this.accountOperationService = accountOperationService;
    }

    @GetMapping(value = Web.Endpoint.BRANCH_ROOT)
    public String listBranches(Model model, BranchDto branchDTO) {

        listingAndConfigTitle(model);
        return Web.View.BRANCH_LIST;
    }

    @PostMapping(value = Web.Endpoint.BRANCH_ROOT)
    public String addNewBranch(Model model, @Valid BranchDto branchDto, BindingResult binding,
                               @RequestParam(required = false, defaultValue = "0") double assetKMF,
                               @RequestParam(required = false, defaultValue = "0") double assetEUR,
                               @RequestParam(required = false, defaultValue = "0") double assetUSD,
                               RedirectAttributes redirect) {

        if(binding.hasErrors() || assetKMF < 0 || assetEUR < 0 || assetUSD < 0) {
            listingAndConfigTitle(model);
            model.addAttribute(Web.MessageTag.ERROR, "Invalid information !");
            return Web.View.BRANCH_LIST;
        }

        double[] vaultsAssets = new double[]{assetKMF, assetEUR, assetUSD};
        branchFacadeService.create(branchDto, vaultsAssets);
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "New branch added successfully !");

        return "redirect:" + Web.View.BRANCH_LIST;
    }

    @GetMapping(value = Web.Endpoint.BRANCH_ACCOUNTS)
    public String listBranchAccounts(Model model, @RequestParam(required = false) Long code) {

        List< AccountDto> vaults = List.of(
                AccountDto.builder().branch(new BranchDto()).build(),
                AccountDto.builder().branch(new BranchDto()).build(),
                AccountDto.builder().branch(new BranchDto()).build()
        );
        List<LedgerDto> ledgers = Collections.emptyList();

        if(code != null) {
            vaults = branchFacadeService.findAllVaultsByBranchId(code);
            ledgers = branchFacadeService.findAllLedgersByBranchId(code);
        }

        return configAttributesAndRedirectToBranchAccounts(model, vaults, ledgers);
    }

    @PostMapping(value = Web.Endpoint.BRANCH_ACCOUNTS)
    public String adjustVault(Model model, @RequestParam(required = false) Long code,
                              @RequestParam(required = false) String currency,
                              @RequestParam(required = false, defaultValue = "0") double amount,
                              @RequestParam(required = false) String operationType) {

        List< AccountDto> vaults = List.of(
                AccountDto.builder().branch(new BranchDto()).build(),
                AccountDto.builder().branch(new BranchDto()).build(),
                AccountDto.builder().branch(new BranchDto()).build()
        );
        List<LedgerDto> ledgers = Collections.emptyList();

        if(amount <= 0) {
            model.addAttribute(Web.MessageTag.ERROR, "Amount must be positive !");
            return configAttributesAndRedirectToBranchAccounts(model, vaults, ledgers);
        }

        //TODO: find vault by branch and currency
        AccountDto vault = branchFacadeService.findVaultByBranchIdAndCurrency(code, currency);

        //TODO: make the operation
        if(operationType.equalsIgnoreCase("debit"))
            accountOperationService.debit(vault, amount);
        else accountOperationService.credit(vault, amount);

        //TODO: retrieve info
        vaults = branchFacadeService.findAllVaultsByBranchId(code);
        ledgers = branchFacadeService.findAllLedgersByBranchId(code);

        return configAttributesAndRedirectToBranchAccounts(model, vaults, ledgers);
    }

    private String configAttributesAndRedirectToBranchAccounts(Model model, List<AccountDto> vaults, List<LedgerDto> ledgers) {
        configPageTitle(model, Web.Menu.Bank.Branches.ACCOUNTS);

        model.addAttribute("branches", branchFacadeService.findAll());
        model.addAttribute("vaults", vaults);
        model.addAttribute("ledgers", ledgers);

        return Web.View.BRANCH_ACCOUNTS;
    }

    private void listingAndConfigTitle(Model model) {
        configPageTitle(model, Web.Menu.Bank.Branches.ROOT);
        model.addAttribute("branches", StatedObjectFormatter.format(branchFacadeService.findAll()));
    }

    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Bank.MENU, Web.Menu.Bank.Branches.SUB_MENU};
    }
}