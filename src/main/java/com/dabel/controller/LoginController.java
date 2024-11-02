package com.dabel.controller;

import com.dabel.constant.Web;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class LoginController {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private int remainingLoginAttempts;
    private String badCredentialsErrorMessage;


    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error, Model model, HttpServletRequest request) {

        if (error != null) {
            String message = getSecurityLastExceptionMessage(request);
            model.addAttribute(Web.MessageTag.ERROR, message);

            int failedAttempts = MAX_LOGIN_ATTEMPTS - remainingLoginAttempts;

            if(badCredentialsErrorMessage != null && badCredentialsErrorMessage.equalsIgnoreCase("Les identifications sont erronées")
                    && failedAttempts >= 0)
                model.addAttribute("remainingAttempts", failedAttempts);
        }

        return "login";
    }

    private String getSecurityLastExceptionMessage(HttpServletRequest request) {
        Exception exception = (Exception) request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        return Optional.ofNullable(exception).map(Throwable::getMessage).orElse("");
    }

    public void setRemainingLoginAttempts(int remainingAttempts) {
        remainingLoginAttempts = remainingAttempts;
    }

    public void setBadCredentialsErrorMessage(String badCredentialsErrorMessage) {
        this.badCredentialsErrorMessage = badCredentialsErrorMessage;
    }
}
