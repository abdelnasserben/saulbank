package com.dabel.controller;

import com.dabel.app.StatedObjectFormatter;
import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.App;
import com.dabel.dto.BranchDto;
import com.dabel.dto.CustomerDto;
import com.dabel.service.branch.BranchFacadeService;
import com.dabel.service.customer.CustomerFacadeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CustomerController implements PageTitleConfig {

    private final CustomerFacadeService customerFacadeService;
    private final BranchFacadeService branchFacadeService;

    public CustomerController(CustomerFacadeService customerFacadeService, BranchFacadeService branchFacadeService) {
        this.customerFacadeService = customerFacadeService;
        this.branchFacadeService = branchFacadeService;
    }

    @GetMapping(value = App.Endpoint.CUSTOMER_LIST)
    public String listingCustomers(Model model) {

        configPageTitle(model, App.Menu.Customer.LIST);
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

    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{App.Menu.Customer.MENU, null};
    }
}
