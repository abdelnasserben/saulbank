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

    private static final String USER_ADDED_SUCCESS_MESSAGE = "New user added successfully !";
    private static final String USER_INFO_CHANGED_SUCCESS_MESSAGE = "User info successfully changed!";
    private static final String USERNAME_CHANGED_SUCCESS_MESSAGE = "Username successfully changed.";
    private static final String PASSWORD_CHANGED_SUCCESS_MESSAGE = "Password successfully changed!";
    private static final String USER_ROLE_CHANGED_SUCCESS_MESSAGE = "User role successfully changed.";
    private static final String USER_BRANCH_CHANGED_SUCCESS_MESSAGE = "User branch successfully changed.";
    private static final String INVALID_INFORMATION_ERROR_MESSAGE = "Invalid information !";
    private static final String USERNAME_TAKEN_WARNING_MESSAGE = "Username already exists";
    private static final String CURRENT_PASSWORD_INCORRECT_ERROR_MESSAGE = "Current password is incorrect!";
    private static final String PASSWORD_CONFIRMATION_ERROR_MESSAGE = "New password and confirmation do not match or are empty!";

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
    public String showUserDetails(Model model, @PathVariable String username) {

        UserDto user = userService.findByUsername(username);
        configPageTitle(model, "User Details");
        model.addAttribute("user", StatedObjectFormatter.format(user));
        model.addAttribute("loginSessions", StatedObjectFormatter.format(userService.getLoginLogs(user)));
        model.addAttribute("userLogs", userService.getLogs(user));
        model.addAttribute("branches", branchFacadeService.getAll());

        return Web.View.USERS_DETAILS;
    }

    @PostMapping(value = Web.Endpoint.USERS)
    public String addUser(Model model, @Valid UserDto userDto, BindingResult binding,
                          @RequestParam(required = false, defaultValue = "0") long branchCode,
                          RedirectAttributes redirectAttributes) {

        if(binding.hasErrors() || branchCode <= 0) {
            prepareUserListPage(model);
            model.addAttribute(Web.MessageTag.ERROR, INVALID_INFORMATION_ERROR_MESSAGE);
            return Web.View.USERS;
        }

        //TODO: set user branch and save
        userDto.setBranch(branchFacadeService.getById(branchCode));
        userService.create(userDto);
        redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, USER_ADDED_SUCCESS_MESSAGE);

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
            redirect.addFlashAttribute(Web.MessageTag.SUCCESS, USER_INFO_CHANGED_SUCCESS_MESSAGE);

        } else redirect.addFlashAttribute(Web.MessageTag.ERROR, INVALID_INFORMATION_ERROR_MESSAGE);

        return redirectToUser(username);
    }

    @PutMapping(value = Web.Endpoint.USERS_UPDATE_USERNAME + "/{username}")
    public String updateUsername(@PathVariable String username,
                                 @RequestParam(defaultValue = "") String newUsername,
                                 RedirectAttributes redirectAttributes,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        UserDto currentUser = userService.getAuthenticated();
        UserDto userDto = userService.findByUsername(username);

        if (!newUsername.isEmpty() && !newUsername.isBlank()) {

            if(!userService.isUsernameTaken(newUsername)) {

                userService.updateUsername(userDto, newUsername);

                String logoutIfCurrentUser = handleLogoutIfCurrentUser(currentUser, username, request, response, redirectAttributes, USERNAME_CHANGED_SUCCESS_MESSAGE + ". Please log in again.");
                if (logoutIfCurrentUser != null)
                    return logoutIfCurrentUser;

                redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, USERNAME_CHANGED_SUCCESS_MESSAGE);
                return redirectToUser(newUsername);

            } else redirectAttributes.addFlashAttribute(Web.MessageTag.WARNING, USERNAME_TAKEN_WARNING_MESSAGE);

        } else redirectAttributes.addFlashAttribute(Web.MessageTag.ERROR, INVALID_INFORMATION_ERROR_MESSAGE);

        return redirectToUser(username);
    }

    @PutMapping(value = Web.Endpoint.USERS_UPDATE_PASSWORD + "/{username}")
    public String updateUserPassword(@PathVariable String username,
                                     @RequestParam String currentPassword,
                                     @RequestParam String newPassword,
                                     @RequestParam String confirmPassword,
                                     RedirectAttributes redirectAttributes,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {

        UserDto currentUser = userService.getAuthenticated();
        UserDto user = userService.findByUsername(username);

        if (!newPassword.isEmpty() && !newPassword.isBlank() && newPassword.equals(confirmPassword)) {
            if (userService.isPasswordValid(user, currentPassword)) {
                userService.updatePassword(user, newPassword);

                String logoutIfCurrentUser = handleLogoutIfCurrentUser(currentUser, username, request, response, redirectAttributes, PASSWORD_CHANGED_SUCCESS_MESSAGE + ". Please log in again.");
                if (logoutIfCurrentUser != null)
                    return logoutIfCurrentUser;

                redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, PASSWORD_CHANGED_SUCCESS_MESSAGE);

            } else redirectAttributes.addFlashAttribute(Web.MessageTag.ERROR, CURRENT_PASSWORD_INCORRECT_ERROR_MESSAGE);

        } else redirectAttributes.addFlashAttribute(Web.MessageTag.ERROR, PASSWORD_CONFIRMATION_ERROR_MESSAGE);

        return redirectToUser(username);
    }

    @PutMapping(value = Web.Endpoint.USERS_UPDATE_ROLE + "/{username}")
    public String updateUserRole(@PathVariable String username, @RequestParam UserRole newRole,
                                 RedirectAttributes redirectAttributes,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        UserDto currentUser = userService.getAuthenticated();
        UserDto userDto = userService.findByUsername(username);

        if (!userDto.getRole().equalsIgnoreCase(newRole.name())) {
            userService.updateRole(userDto, newRole.name());

            String logoutIfCurrentUser = handleLogoutIfCurrentUser(currentUser, username, request, response, redirectAttributes, USER_ROLE_CHANGED_SUCCESS_MESSAGE + ". Please log in again.");
            if (logoutIfCurrentUser != null)
                return logoutIfCurrentUser;

            redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, USER_ROLE_CHANGED_SUCCESS_MESSAGE);

        } else redirectAttributes.addFlashAttribute(Web.MessageTag.WARNING, "The user already holds the '" + newRole.name() + "' role. No changes were made.");

        return redirectToUser(username);
    }

    @PutMapping(value = Web.Endpoint.USERS_UPDATE_BRANCH + "/{username}")
    public String updateUserBranch(
            @PathVariable String username,
            @RequestParam(required = false, defaultValue = "0") long newBranchCode,
            RedirectAttributes redirect) {

        UserDto user = userService.findByUsername(username);

        if (newBranchCode <= 0) {
            redirect.addFlashAttribute(Web.MessageTag.ERROR, INVALID_INFORMATION_ERROR_MESSAGE);
            return redirectToUser(username);
        }

        BranchDto branchDto = branchFacadeService.getById(newBranchCode);

        if (user.getBranch().getBranchId() == newBranchCode) {
            redirect.addFlashAttribute(Web.MessageTag.WARNING, "The user is already assigned to the '" + branchDto.getBranchName() + "' branch. No changes were made.");
            return redirectToUser(username);
        }

        user.setBranch(branchDto);
        userService.save(user);
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, USER_BRANCH_CHANGED_SUCCESS_MESSAGE);

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
        model.addAttribute("branches", branchFacadeService.getAll());
    }

    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Bank.MENU, Web.Menu.Bank.Users.SUB_MENU};
    }
}