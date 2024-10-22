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
import org.springframework.web.bind.annotation.*;
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

    @GetMapping(value = Web.Endpoint.USERS + "/{username}")
    public String userDetails(Model model, @PathVariable String username) {

        UserDto user = userService.findByUsername(username);
        configPageTitle(model, "User Details");
        model.addAttribute("user", StatedObjectFormatter.format(user));
        model.addAttribute("loginSessions", StatedObjectFormatter.format(userService.getLoginLogs(user)));
        model.addAttribute("userLogs", userService.getLogs(user));
        model.addAttribute("branches", branchFacadeService.findAll());

        return Web.View.USERS_DETAILS;
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

    @PutMapping(value = Web.Endpoint.USERS_UPDATE_INFO + "/{username}")
    public String updateUserInfo(Model model,
                                 @PathVariable String username,
                                 @RequestParam String firstName,
                                 @RequestParam String lastName,
                                 @RequestParam(required = false, defaultValue = "0") long branchCode,
                                 RedirectAttributes redirect) {
        //We'll make this implementation better
        UserDto user = userService.findByUsername(username);
        if (!firstName.isBlank() && !lastName.isBlank() && branchCode >= 0) {
            user.setFirstName(firstName);
            user.setLastName(lastName);

            userService.save(user);
            redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "User details successfully changed!");

        } else redirect.addFlashAttribute(Web.MessageTag.ERROR, "Invalid information !");

        return "redirect:" + Web.Endpoint.USERS + "/" + username;
    }

    @PutMapping(value = Web.Endpoint.USERS_UPDATE_NAME + "/{username}")
    public String updateUsername(Model model,
                                 @PathVariable String username,
                                 @RequestParam(defaultValue = "") String profileUsername,
                                 RedirectAttributes redirect) {

        //We'll make this implementation better
        UserDto user = userService.findByUsername(username);

        if(!profileUsername.isEmpty() && !profileUsername.isBlank()) {
            user.setUsername(profileUsername);
            userService.save(user);
            redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Username successfully changed");

        } else redirect.addFlashAttribute(Web.MessageTag.ERROR, "Invalid Information");

        return "redirect:" + Web.Endpoint.USERS + "/" + username;
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