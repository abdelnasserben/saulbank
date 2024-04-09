package com.dabel.controller;


import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.Web;
import com.dabel.dto.PostChequeDto;
import com.dabel.dto.PostChequeRequestDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ChequeController implements PageTitleConfig {

    @GetMapping(value = Web.Endpoint.CHEQUES)
    public String listingCheques(Model model) {

        configPageTitle(model, Web.Menu.Cheque.ROOT);
//        model.addAttribute("cheques", StatedObjectFormatter.format(cardFacadeService.findAllCards()));
        return Web.View.CHEQUES;
    }

    @GetMapping(value = Web.Endpoint.CHEQUE_PAY)
    public String initChequePayment(Model model, PostChequeDto postChequeDto) {
        configPageTitle(model, Web.Menu.Cheque.PAY);

        return Web.View.CHEQUE_PAY;
    }

    @PostMapping(value = Web.Endpoint.CHEQUE_PAY)
    public String handleInitChequePayment(Model model, @Valid PostChequeDto postChequeDto, BindingResult binding, RedirectAttributes redirect) {

        if(binding.hasErrors()) {
            configPageTitle(model, Web.Menu.Cheque.PAY);
            model.addAttribute(Web.MessageTag.ERROR, "Invalid information!");

            return Web.View.CHEQUE_PAY;
        }

        //TODO: save init cheque payment

        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Cheque payment successfully initiated.");

        return "redirect:" + Web.Endpoint.CHEQUE_PAY;
    }

    @GetMapping(value = Web.Endpoint.CHEQUE_REQUESTS)
    public String listingRequests(Model model, PostChequeRequestDto postChequeRequestDto) {

        configPageTitle(model, Web.Menu.Cheque.REQUESTS);
//        model.addAttribute("chequeRequests", StatedObjectFormatter.format(cardFacadeService.findAllCardRequests()));

        return Web.View.CHEQUE_REQUESTS;
    }

    @PostMapping(value = Web.Endpoint.CHEQUE_REQUESTS)
    public String handleChequeRequest(Model model, @Valid PostChequeRequestDto postChequeRequestDto, BindingResult binding, RedirectAttributes redirect) {

        if(binding.hasErrors()) {
            configPageTitle(model, Web.Menu.Cheque.REQUESTS);
            model.addAttribute(Web.MessageTag.ERROR, "Invalid request application information !");
            return Web.View.CHEQUE_REQUESTS;
        }

        //TODO: save sending request

        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Cheque request successfully sent !");

        return "redirect:" + Web.Endpoint.CHEQUE_REQUESTS;
    }


    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Cheque.MENU, null};
    }
}
