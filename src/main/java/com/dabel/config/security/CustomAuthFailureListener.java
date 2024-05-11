package com.dabel.config.security;

import com.dabel.dto.UserDto;
import com.dabel.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    private final UserService userService;

    @Autowired
    public CustomAuthFailureListener(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {

        UserDto userDto = userService.findByUsername((String) event.getAuthentication().getPrincipal());
        int MAX_ATTEMPTS = 5;

        //TODO: verify failed attempts count
        if (userDto.getFailedLoginAttempts() + 1 >= MAX_ATTEMPTS)
            userService.lockAccount(userDto.getUsername());

        //TODO: increment failed attempts
        userService.incrementFailedLoginAttempts(userDto.getUsername());
    }
}
