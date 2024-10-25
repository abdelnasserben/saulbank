package com.dabel.controller;

import com.dabel.app.StatedObjectFormatter;
import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.UserRole;
import com.dabel.constant.Web;
import com.dabel.dto.BranchDto;
import com.dabel.dto.UserDto;
import com.dabel.service.branch.BranchFacadeService;
import com.dabel.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

        prepareUserListPage(model);
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

        if(binding.hasErrors() || branchCode <= 0) {
            prepareUserListPage(model);
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
    public String updateUserInfo(@PathVariable String username,
                                 @RequestParam String firstName,
                                 @RequestParam String lastName,
                                 RedirectAttributes redirect) {

        UserDto userDto = userService.findByUsername(username);
        if (!firstName.isBlank() && !lastName.isBlank()) {
            userDto.setFirstName(firstName);
            userDto.setLastName(lastName);

            userService.save(userDto);
            redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "User info successfully changed!");

        } else redirect.addFlashAttribute(Web.MessageTag.ERROR, "Invalid information !");

        return redirectToUser(username);
    }

    @PutMapping(value = Web.Endpoint.USERS_UPDATE_USERNAME + "/{username}")
    public String updateUsername(@PathVariable String username,
                                 @RequestParam(defaultValue = "") String newUsername,
                                 RedirectAttributes redirect,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        UserDto currentUser = userService.getAuthenticated();
        UserDto userDto = userService.findByUsername(username);

        if (!newUsername.isEmpty() && !newUsername.isBlank()) {

            if(!userService.isUsernameTaken(newUsername)) {

                userService.updateUsername(userDto, newUsername);

                String logoutIfCurrentUser = handleLogoutIfCurrentUser(currentUser, username, request, response, redirect, "Username successfully changed. Please log in again.");
                if (logoutIfCurrentUser != null)
                    return logoutIfCurrentUser;

                redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Username successfully changed.");
                return redirectToUser(newUsername);

            } else redirect.addFlashAttribute(Web.MessageTag.WARNING, "Username already exists");

        } else redirect.addFlashAttribute(Web.MessageTag.ERROR, "Invalid Information");

        return redirectToUser(username);
    }

    @PutMapping(value = Web.Endpoint.USERS_UPDATE_PASSWORD + "/{username}")
    public String updateUserPassword(@PathVariable String username,
                                     @RequestParam String currentPassword,
                                     @RequestParam String newPassword,
                                     @RequestParam String confirmPassword,
                                     RedirectAttributes redirect,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {

        UserDto currentUser = userService.getAuthenticated();
        UserDto user = userService.findByUsername(username);

        if (!newPassword.isEmpty() && !newPassword.isBlank() && newPassword.equals(confirmPassword)) {
            if (userService.isPasswordValid(user, currentPassword)) {
                userService.updatePassword(user, newPassword);

                String logoutIfCurrentUser = handleLogoutIfCurrentUser(currentUser, username, request, response, redirect, "Password successfully changed. Please log in again.");
                if (logoutIfCurrentUser != null)
                    return logoutIfCurrentUser;

                redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Password successfully changed!");

            } else redirect.addFlashAttribute(Web.MessageTag.ERROR, "Current password is incorrect!");

        } else redirect.addFlashAttribute(Web.MessageTag.ERROR, "New password and confirmation do not match or are empty!");

        return redirectToUser(username);
    }

    @PutMapping(value = Web.Endpoint.USERS_UPDATE_ROLE + "/{username}")
    public String updateUserRole(@PathVariable String username, @RequestParam UserRole newRole,
                                 RedirectAttributes redirect,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        UserDto currentUser = userService.getAuthenticated();
        UserDto userDto = userService.findByUsername(username);

        if (!userDto.getRole().equalsIgnoreCase(newRole.name())) {
            userService.updateRole(userDto, newRole.name());

            String logoutIfCurrentUser = handleLogoutIfCurrentUser(currentUser, username, request, response, redirect, "User role successfully changed. Please log in again.");
            if (logoutIfCurrentUser != null)
                return logoutIfCurrentUser;

            redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "User role successfully changed.");

        } else redirect.addFlashAttribute(Web.MessageTag.WARNING, "The user already holds the '" + newRole.name() + "' role. No changes were made.");

        return redirectToUser(username);
    }

    @PutMapping(value = Web.Endpoint.USERS_UPDATE_BRANCH + "/{username}")
    public String updateUserBranch(
            @PathVariable String username,
            @RequestParam(required = false, defaultValue = "0") long newBranchCode,
            RedirectAttributes redirect) {

        UserDto user = userService.findByUsername(username);

        if (newBranchCode <= 0) {
            redirect.addFlashAttribute(Web.MessageTag.ERROR, "Invalid Information");
            return redirectToUser(username);
        }

        BranchDto branchDto = branchFacadeService.findById(newBranchCode);

        if (user.getBranch().getBranchId() == newBranchCode) {
            redirect.addFlashAttribute(Web.MessageTag.WARNING, "The user is already assigned to the '" + branchDto.getBranchName() + "' branch. No changes were made.");
            return redirectToUser(username);
        }

        user.setBranch(branchDto);
        userService.save(user);
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "User branch successfully changed.");

        return redirectToUser(username);
    }

    private String redirectToUser(String username) {
        return "redirect:" + Web.Endpoint.USERS + "/" + username;
    }

    private String handleLogoutIfCurrentUser(UserDto currentUser, String username, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirect, String successMessage) {
        if (currentUser.getUsername().equals(username)) {
            userService.logoutUser(request, response);
            redirect.addFlashAttribute(Web.MessageTag.SUCCESS, successMessage);
            return "redirect:/login";
        }
        return null;
    }

    private void prepareUserListPage(Model model) {
        configPageTitle(model, Web.Menu.Bank.Users.ROOT);
        model.addAttribute("users", StatedObjectFormatter.format(userService.findAll()));
        model.addAttribute("branches", branchFacadeService.findAll());
    }

    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Bank.MENU, Web.Menu.Bank.Users.SUB_MENU};
    }
}