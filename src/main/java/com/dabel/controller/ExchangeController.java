package com.dabel.controller;

import com.dabel.app.StatedObjectFormatter;
import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.Web;
import com.dabel.dto.ExchangeDto;
import com.dabel.service.exchange.ExchangeFacadeService;
import com.dabel.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ExchangeController implements PageTitleConfig {

    private static final String SUCCESS_INITIATE_MESSAGE = "Exchange successfully initiated";
    private static final String SUCCESS_APPROVE_MESSAGE = "Exchange successfully approved!";
    private static final String SUCCESS_REJECT_MESSAGE = "Exchange successfully rejected!";
    private static final String ERROR_INVALID_INFORMATION = "Invalid information!";
    private static final String ERROR_REJECT_REASON_REQUIRED = "Reject reason is required!";

    private final ExchangeFacadeService exchangeFacadeService;
    private final UserService userService;

    @Autowired
    public ExchangeController(ExchangeFacadeService exchangeFacadeService, UserService userService) {
        this.exchangeFacadeService = exchangeFacadeService;
        this.userService = userService;
    }

    @GetMapping(value = Web.Endpoint.EXCHANGES)
    public String listExchanges(Model model) {
        configPageTitle(model, Web.Menu.Exchange.ROOT);
        model.addAttribute("exchanges", StatedObjectFormatter.format(exchangeFacadeService.getAll()));

        return Web.View.EXCHANGES;
    }

    @GetMapping(value = Web.Endpoint.EXCHANGES + "/{exchangeId}")
    public String showExchangeDetails(@PathVariable Long exchangeId, Model model) {

        ExchangeDto exchange = exchangeFacadeService.getById(exchangeId);

        configPageTitle(model, "Exchange Details");
        model.addAttribute("exchange", StatedObjectFormatter.format(exchange));
        return Web.View.EXCHANGE_DETAILS;
    }

    @GetMapping(value = Web.Endpoint.EXCHANGE_INIT)
    public String initializeExchange(Model model, ExchangeDto exchangeDto) {
        configPageTitle(model, Web.Menu.Exchange.INIT);

        return Web.View.EXCHANGE_INIT;
    }

    @PostMapping(value = Web.Endpoint.EXCHANGE_INIT)
    public String handleInitializeExchange(Model model, @Valid ExchangeDto exchangeDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            configPageTitle(model, Web.Menu.Exchange.INIT);
            model.addAttribute(Web.MessageTag.ERROR, ERROR_INVALID_INFORMATION);
            return Web.View.EXCHANGE_INIT;
        }

        exchangeDto.setBranch(userService.getAuthenticated().getBranch());
        exchangeFacadeService.init(exchangeDto);
        redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, SUCCESS_INITIATE_MESSAGE);

        return "redirect:" + Web.Endpoint.EXCHANGE_INIT;
    }

    @PostMapping(value = Web.Endpoint.EXCHANGE_APPROVE + "/{exchangeId}")
    public String approveExchange(@PathVariable Long exchangeId, RedirectAttributes redirectAttributes) {

        exchangeFacadeService.approve(exchangeId);
        redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, SUCCESS_APPROVE_MESSAGE);
        return redirectToExchangeDetails(exchangeId);
    }

    @PostMapping(value = Web.Endpoint.EXCHANGE_REJECT + "/{exchangeId}")
    public String rejectExchange(@PathVariable Long exchangeId, @RequestParam String rejectReason, RedirectAttributes redirect) {

        if(rejectReason.isBlank())
            redirect.addFlashAttribute(Web.MessageTag.ERROR, ERROR_REJECT_REASON_REQUIRED);
        else {
            redirect.addFlashAttribute(Web.MessageTag.SUCCESS, SUCCESS_REJECT_MESSAGE);
            exchangeFacadeService.reject(exchangeId, rejectReason);
        }

        return redirectToExchangeDetails(exchangeId);
    }

    private String redirectToExchangeDetails(Long exchangeId) {
        return "redirect:" + Web.Endpoint.EXCHANGES + "/" + exchangeId;
    }

    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Exchange.MENU, null};
    }
}
