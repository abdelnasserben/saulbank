package com.dabel.config;

import com.dabel.constant.Web;
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
public final class CustomExceptionHandler {

    @ExceptionHandler(value = {BalanceInsufficientException.class, IllegalOperationException.class,})
    public String basicHandler(HttpServletRequest request, Exception ex, RedirectAttributes redirect) {
        return redirection(redirect, request.getRequestURI(), ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String resourceNotFoundHandler(HttpServletRequest request, Exception ex, RedirectAttributes redirect) {

        List<String> infiniteRedirectionUrl = List.of(
                Web.Endpoint.CUSTOMER_ROOT,
                Web.Endpoint.TRANSACTION_ROOT,
                Web.Endpoint.EXCHANGE_ROOT,
                Web.Endpoint.CARD_ROOT,
                Web.Endpoint.CARD_REQUEST_ROOT,
                Web.Endpoint.LOAN_ROOT,
                Web.Endpoint.ACCOUNT_ROOT
        );

        String suspectedUrl = request.getRequestURI().substring(0, request.getRequestURI().lastIndexOf("/"));

        try {
            Long.parseLong(request.getRequestURI().substring(suspectedUrl.length() + 1));
            if(infiniteRedirectionUrl.contains(suspectedUrl))
                return "redirect:" + Web.Endpoint.PAGE_404;

        } catch (NumberFormatException ignored) {}

        return redirection(redirect, request.getRequestURI(), ex.getMessage());
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public String dataIntegrityViolationHandler(HttpServletRequest request, Exception ex, RedirectAttributes redirect) {
        return redirection(redirect, request.getRequestURI(), ex.getMessage().split("(for key)")[0].trim() + "]");
    }

    private String redirection(RedirectAttributes redirect, String url, String message) {
        redirect.addFlashAttribute(Web.MessageTag.ERROR, message);
        return "redirect:" + url;
    }
}
