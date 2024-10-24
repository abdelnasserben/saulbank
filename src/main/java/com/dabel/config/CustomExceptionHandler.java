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

    private static final List<String> SUSPECTED_INFINITE_REDIRECTION_URLS = List.of(
            Web.Endpoint.CUSTOMERS,
            Web.Endpoint.TRANSACTIONS,
            Web.Endpoint.EXCHANGES,
            Web.Endpoint.CARDS,
            Web.Endpoint.CARD_REQUESTS,
            Web.Endpoint.LOANS,
            Web.Endpoint.LOAN_REQUESTS,
            Web.Endpoint.CHEQUES,
            Web.Endpoint.CHEQUE_REQUESTS,
            Web.Endpoint.ACCOUNTS,
            Web.Endpoint.ACCOUNT_AFFILIATION
    );

    @ExceptionHandler(value = {BalanceInsufficientException.class, IllegalOperationException.class,})
    public String basicHandler(HttpServletRequest request, Exception ex, RedirectAttributes redirect) {

        return redirection(redirect, request.getRequestURI(), ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String resourceNotFoundHandler(HttpServletRequest request, Exception ex, RedirectAttributes redirect) {

        String requestURI = request.getRequestURI();
        String suspectedUrl = requestURI.substring(0, requestURI.lastIndexOf("/"));

        if (isSuspectedInfiniteRedirection(suspectedUrl, requestURI)) {
            return "redirect:" + Web.Endpoint.PAGE_404;
        }

        return redirection(redirect, request.getRequestURI(), ex.getMessage());
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public String dataIntegrityViolationHandler(HttpServletRequest request, Exception ex, RedirectAttributes redirect) {
        return redirection(redirect, request.getRequestURI(), "data integrity violation");
    }

    private boolean isSuspectedInfiniteRedirection(String suspectedUrl, String requestURI) {
        try {
            Long.parseLong(requestURI.substring(suspectedUrl.length() + 1));
            return SUSPECTED_INFINITE_REDIRECTION_URLS.contains(suspectedUrl);
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private String redirection(RedirectAttributes redirect, String url, String message) {
        redirect.addFlashAttribute(Web.MessageTag.ERROR, message);
        return "redirect:" + url;
    }
}
