package com.dabel.config;

import com.dabel.constant.Web;
import com.dabel.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;


@Component
public class AuthenticatedUserModelAndRequestLoggingInterceptor implements HandlerInterceptor {

    private final UserService userService;

    public AuthenticatedUserModelAndRequestLoggingInterceptor(UserService userService) {
        this.userService = userService;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        if(modelAndView != null) {

            //TODO: adding authenticated user in model attributes
            modelAndView.addObject("authenticatedUser", userService.getAuthenticated());

            //TODO: prepare to save user log
            String url = request.getRequestURL().toString();
            String method = request.getMethod();
            HttpStatus status = HttpStatus.valueOf(response.getStatus());

            Map<String, Object> viewModel = modelAndView.getModel();
            if(viewModel.containsKey(Web.MessageTag.ERROR))
                status = HttpStatus.BAD_REQUEST;

            if(url.contains(Web.Endpoint.PAGE_404))
                status = HttpStatus.NOT_FOUND;

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if(auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                userService.saveLog(method, String.valueOf(status), url);
            }
        }
    }
}

