package com.dabel.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UserLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        saveUserRequestLog(request, response);
        filterChain.doFilter(request, response);
    }

    private void saveUserRequestLog(HttpServletRequest request, HttpServletResponse response) {

        String username = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication()).map(Principal::getName).orElse("N/A");
        String method = request.getMethod();
        String status = HttpStatus.valueOf(response.getStatus()).name();
        String url = request.getRequestURL().toString();

        Pattern pattern = Pattern.compile("/(css|js|assets)");
        Matcher matcher = pattern.matcher(url);

        if(!matcher.find() && method.equalsIgnoreCase("POST"))
            System.err.printf("%s - %s - %s - %s\n", username, method, status, url);
    }
}
