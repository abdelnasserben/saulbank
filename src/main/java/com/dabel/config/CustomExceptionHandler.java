package com.dabel.config;

import com.dabel.constant.App;
import com.dabel.exception.BalanceInsufficientException;
import com.dabel.exception.IllegalOperationException;
import com.dabel.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(value = {BalanceInsufficientException.class, IllegalOperationException.class,})
    public String basicHandler(HttpServletRequest request, Exception ex, RedirectAttributes redirect) {
        return redirection(redirect, request.getRequestURI(), ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String resourceNotFoundHandler(HttpServletRequest request, Exception ex, RedirectAttributes redirect) {

        List<String> infiniteRedirectionViews = List.of(
                App.Endpoint.CUSTOMER_ROOT,
                App.Endpoint.TRANSACTION_ROOT,
                App.Endpoint.EXCHANGE_ROOT
        );

        String view = request.getRequestURI().substring(0, request.getRequestURI().lastIndexOf("/"));

        if(infiniteRedirectionViews.contains(view)) {
            return "redirect:" + App.Endpoint.PAGE_404;
        }

        return redirection(redirect, request.getRequestURI(), ex.getMessage());
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public String dataIntegrityViolationHandler(HttpServletRequest request, Exception ex, RedirectAttributes redirect) {
        return redirection(redirect, request.getRequestURI(), ex.getMessage().split("(for key)")[0].trim() + "]");
    }

    private String redirection(RedirectAttributes redirect, String url, String message) {
        redirect.addFlashAttribute(App.MessageTag.ERROR, message);
        return "redirect:" + url;
    }
}
