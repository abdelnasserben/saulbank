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

    private final ExchangeFacadeService exchangeFacadeService;
    private final UserService userService;

    @Autowired
    public ExchangeController(ExchangeFacadeService exchangeFacadeService, UserService userService) {
        this.exchangeFacadeService = exchangeFacadeService;
        this.userService = userService;
    }

    @GetMapping(value = Web.Endpoint.EXCHANGES)
    public String listingExchanges(Model model) {
        configPageTitle(model, Web.Menu.Exchange.ROOT);
        model.addAttribute("exchanges", StatedObjectFormatter.format(exchangeFacadeService.findAll()));

        return Web.View.EXCHANGES;
    }

    @GetMapping(value = Web.Endpoint.EXCHANGES + "/{exchangeId}")
    public String exchangeDetails(@PathVariable Long exchangeId, Model model) {

        ExchangeDto exchange = exchangeFacadeService.findById(exchangeId);

        configPageTitle(model, "Exchange Details");
        model.addAttribute("exchange", StatedObjectFormatter.format(exchange));
        return Web.View.EXCHANGE_DETAILS;
    }

    @GetMapping(value = Web.Endpoint.EXCHANGE_INIT)
    public String initExchange(Model model, ExchangeDto exchangeDto) {
        configPageTitle(model, Web.Menu.Exchange.INIT);

        return Web.View.EXCHANGE_INIT;
    }

    @PostMapping(value = Web.Endpoint.EXCHANGE_INIT)
    public String initExchange(Model model, @Valid ExchangeDto exchangeDto, BindingResult binding, RedirectAttributes redirect) {

        if(binding.hasErrors()) {
            configPageTitle(model, Web.Menu.Exchange.INIT);
            model.addAttribute(Web.MessageTag.ERROR, "Invalid information !");
            return Web.View.EXCHANGE_INIT;
        }

        exchangeDto.setBranch(userService.getAuthenticated().getBranch());

        exchangeFacadeService.init(exchangeDto);

        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Exchange successfully initiated");
        return "redirect:" + Web.Endpoint.EXCHANGE_INIT;
    }

    @PostMapping(value = Web.Endpoint.EXCHANGE_APPROVE + "/{exchangeId}")
    public String approveExchange(@PathVariable Long exchangeId, RedirectAttributes redirect) {

        exchangeFacadeService.approve(exchangeId);
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Exchange successfully approved!");

        return "redirect:" + Web.Endpoint.EXCHANGES + "/" + exchangeId;
    }

    @PostMapping(value = Web.Endpoint.EXCHANGE_REJECT + "/{exchangeId}")
    public String rejectExchange(@PathVariable Long exchangeId, @RequestParam String rejectReason, RedirectAttributes redirect) {

        if(rejectReason.isBlank())
            redirect.addFlashAttribute(Web.MessageTag.ERROR, "Reject reason is mandatory!");
        else {
            redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Exchange successfully rejected!");
            exchangeFacadeService.reject(exchangeId, rejectReason);
        }

        return "redirect:" + Web.Endpoint.EXCHANGES + "/" + exchangeId;
    }


    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Exchange.MENU, null};
    }
}
