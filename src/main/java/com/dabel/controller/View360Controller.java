package com.dabel.controller;

import com.dabel.app.StatedObjectFormatter;
import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.Web;
import com.dabel.dto.AccountDto;
import com.dabel.dto.BranchDto;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.branch.BranchFacadeService;
import com.dabel.service.transaction.TransactionFacadeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class View360Controller implements PageTitleConfig {

    private final BranchFacadeService branchFacadeService;
    private final AccountFacadeService accountFacadeService;
    private final TransactionFacadeService transactionFacadeService;

    public View360Controller(BranchFacadeService branchFacadeService, AccountFacadeService accountFacadeService, TransactionFacadeService transactionFacadeService) {
        this.branchFacadeService = branchFacadeService;
        this.accountFacadeService = accountFacadeService;
        this.transactionFacadeService = transactionFacadeService;
    }

    /** For branches **/

    @GetMapping(value = Web.Endpoint.VIEW360_BRANCHES)
    public String listBranches(Model model, BranchDto branchDTO) {

        branchListingAndConfigTitle(model);
        return Web.View.BRANCHES;
    }

    @PostMapping(value = Web.Endpoint.VIEW360_BRANCHES)
    public String addNewBranch(Model model, @Valid BranchDto branchDto, BindingResult binding,
                               @RequestParam(required = false, defaultValue = "0") double assetKMF,
                               @RequestParam(required = false, defaultValue = "0") double assetEUR,
                               @RequestParam(required = false, defaultValue = "0") double assetUSD,
                               RedirectAttributes redirect) {

        if(binding.hasErrors() || assetKMF < 0 || assetEUR < 0 || assetUSD < 0) {
            branchListingAndConfigTitle(model);
            model.addAttribute(Web.MessageTag.ERROR, "Invalid information !");
            return Web.View.BRANCHES;
        }

        double[] vaultsAssets = new double[]{assetKMF, assetEUR, assetUSD};
        branchFacadeService.create(branchDto, vaultsAssets);
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "New branch added successfully !");

        return "redirect:" + Web.Endpoint.VIEW360_BRANCHES;
    }

    @GetMapping(value = Web.Endpoint.VIEW360_VAULT_GL)
    public String listBranchAccounts(Model model, @RequestParam(name="code", required = false) Long code) {

        if(code != null) {
            try {
                BranchDto branchDto = branchFacadeService.findById(code);
                model.addAttribute("branch", StatedObjectFormatter.format(branchDto));
                model.addAttribute("vaults", branchFacadeService.findAllVaultsByBranchId(branchDto.getBranchId()));
                model.addAttribute("ledgers", branchFacadeService.findAllLedgersByBranchId(branchDto.getBranchId()));
            } catch (ResourceNotFoundException ex) {
                model.addAttribute(Web.MessageTag.ERROR, "Branch not found");
            }
        }

        configPageTitle(model, Web.Menu.Bank.View360.VAULT_GL);
        return Web.View.BRANCH_ACCOUNTS;
    }

    @PostMapping(value = Web.Endpoint.VIEW360_VAULT_GL)
    public String adjustVault(Model model, @RequestParam Long code,
                              @RequestParam(required = false) String currency,
                              @RequestParam(required = false, defaultValue = "0") double amount,
                              @RequestParam(required = false) String operationType,
                              RedirectAttributes redirect) {

        BranchDto branchDto = branchFacadeService.findById(code);

        if(amount <= 0) {
            redirect.addFlashAttribute(Web.MessageTag.ERROR, "Amount must be positive !");
        } else {
            //TODO: find branch and his vault by currency
            AccountDto vault = branchFacadeService.findVaultByBranchIdAndCurrency(branchDto.getBranchId(), currency);

            //TODO: make the operation
            if(operationType.equalsIgnoreCase("debit"))
                accountFacadeService.debit(vault, amount);
            else accountFacadeService.credit(vault, amount);

            redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Successful adjustment");
        }

        return String.format("redirect:%s?code=%d", Web.Endpoint.VIEW360_VAULT_GL, code);
    }

    /** For accounts **/
    @GetMapping(value = Web.Endpoint.VIEW360_ACCOUNTS)
    public String listAccounts(Model model) {

        configPageTitle(model, Web.Menu.Bank.View360.ACCOUNTS);
        model.addAttribute("accounts", StatedObjectFormatter.format(accountFacadeService.findAll()));
        return Web.View.VIEW360_ACCOUNTS;
    }

    /** For transactions **/
    @GetMapping(value = Web.Endpoint.VIEW360_TRANSACTIONS)
    public String listingTransactions(Model model) {

        configPageTitle(model, Web.Menu.Bank.View360.TRANSACTIONS);
        model.addAttribute("transactions", StatedObjectFormatter.format(transactionFacadeService.findAll()));
        return Web.View.TRANSACTIONS;
    }

    private void branchListingAndConfigTitle(Model model) {
        configPageTitle(model, Web.Menu.Bank.View360.BRANCHES);
        model.addAttribute("branches", StatedObjectFormatter.format(branchFacadeService.findAll()));
    }

    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Bank.MENU, Web.Menu.Bank.View360.SUB_MENU};
    }
}