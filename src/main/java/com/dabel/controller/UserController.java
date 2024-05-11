package com.dabel.controller;

import com.dabel.app.StatedObjectFormatter;
import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.Web;
import com.dabel.dto.UserDto;
import com.dabel.service.branch.BranchFacadeService;
import com.dabel.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController implements PageTitleConfig {

    private final BranchFacadeService branchFacadeService;
    private final UserService userService;


    @Autowired
    public UserController(BranchFacadeService branchFacadeService, UserService userService) {
        this.branchFacadeService = branchFacadeService;
        this.userService = userService;
    }


    @GetMapping(value = Web.Endpoint.USERS)
    public String listUsers(Model model, UserDto userDto) {

        listingAndConfigTitle(model);
        return Web.View.USERS;
    }

    @PostMapping(value = Web.Endpoint.USERS)
    public String addUser(Model model, @Valid UserDto userDto, BindingResult binding,
                          @RequestParam(required = false, defaultValue = "0") long branchCode,
                          RedirectAttributes redirect) {

        if(binding.hasErrors() || branchCode < 0) {
            listingAndConfigTitle(model);
            model.addAttribute(Web.MessageTag.ERROR, "Invalid information !");
            return Web.View.USERS;
        }

        //TODO: set user branch and save
        userDto.setBranch(branchFacadeService.findById(branchCode));
        userService.create(userDto);
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "New user added successfully !");

        return "redirect:" + Web.Endpoint.USERS;
    }

    private void listingAndConfigTitle(Model model) {
        configPageTitle(model, Web.Menu.Bank.Users.ROOT);
        model.addAttribute("users", StatedObjectFormatter.format(userService.findAll()));
        model.addAttribute("branches", branchFacadeService.findAll());
    }

    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Bank.MENU, Web.Menu.Bank.Users.SUB_MENU};
    }
}