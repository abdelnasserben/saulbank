package com.dabel.config.security;

import com.dabel.controller.LoginController;
import com.dabel.dto.UserDto;
import com.dabel.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CustomAuthenticationEventPublisher implements AuthenticationEventPublisher {

    private final UserService userService;
    private final LoginController loginController;

    @Autowired
    public CustomAuthenticationEventPublisher(UserService userService, LoginController loginController) {
        this.userService = userService;
        this.loginController = loginController;
    }

    @Override
    public void publishAuthenticationSuccess(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserDto userDto = userService.findByUsername(userDetails.getUsername());

        //TODO: update login date and failed attempts
        userDto.setLoginAt(LocalDateTime.now());
        userDto.setFailedLoginAttempts(0);
        userService.save(userDto);
    }

    @Override
    public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
        UserDto userDto = userService.findByUsername((String) authentication.getPrincipal());
        String errorMessage = exception.getMessage();
        loginController.setBadCredentialsErrorMessage(errorMessage);

        //TODO: increment failed attempts
        if(errorMessage.equalsIgnoreCase("Les identifications sont erronées")) {
            userService.incrementFailedLoginAttempts(userDto.getUsername());
            loginController.setRemainingLoginAttempts(userDto.getFailedLoginAttempts() + 1);
        }
    }
}
