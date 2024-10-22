package com.dabel.config.security;

import com.dabel.constant.Status;
import com.dabel.controller.LoginController;
import com.dabel.dto.LoginLogDto;
import com.dabel.dto.UserDto;
import com.dabel.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CustomAuthenticationEventPublisher implements AuthenticationEventPublisher {

    private final UserService userService;
    private final LoginController loginController;
    private final HttpServletRequest request;

    public CustomAuthenticationEventPublisher(UserService userService, LoginController loginController, HttpServletRequest request) {
        this.userService = userService;
        this.loginController = loginController;
        this.request = request;
    }

    @Override
    public void publishAuthenticationSuccess(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserDto userDto = userService.findByUsername(userDetails.getUsername());
        LocalDateTime loginTime = LocalDateTime.now();

        //TODO: update login date and failed attempts
        userDto.setLoginAt(loginTime);
        userDto.setFailedLoginAttempts(0);
        userService.save(userDto);

        //TODO: save login log
        saveLoginLog(userDto, loginTime, Status.SUCCESS);
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

        //TODO: save login log
        saveLoginLog(userDto, null, Status.FAILED);
    }

    private void saveLoginLog(UserDto userDto, LocalDateTime loginTime, Status status) {

        LoginLogDto loginLogDto = LoginLogDto.builder()
                .os(extractOS(request.getHeader("User-Agent")))
                .ipAddress(request.getRemoteAddr())
                .user(userDto)
                .status(status.code())
                .build();

        if(loginTime != null) loginLogDto.setLoginAt(loginTime);

        userService.saveLoginLog(loginLogDto);
    }

    private static String extractOS(String userAgent) {
        Pattern pattern = Pattern.compile("(Windows|Mac OS X|Linux|iPhone|iPad|Android)");
        Matcher matcher = pattern.matcher(userAgent);

        if (!matcher.find())
            return "Unknown OS";

        String os = matcher.group(1);
        return switch (os) {
            case "Windows" -> "Windows";
            case "Mac OS X" -> "Mac OS";
            case "Linux" -> "Linux";
            case "iPhone", "iPad" -> "iOS";
            case "Android" -> "Android";
            default -> "Unknown OS";
        };
    }
}
