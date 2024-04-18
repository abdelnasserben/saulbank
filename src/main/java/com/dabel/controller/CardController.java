package com.dabel.controller;

import com.dabel.app.CardExpirationDateUtils;
import com.dabel.app.StatedObjectFormatter;
import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.Status;
import com.dabel.constant.Web;
import com.dabel.dto.*;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.branch.BranchFacadeService;
import com.dabel.service.card.CardFacadeService;
import com.dabel.service.customer.CustomerFacadeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CardController implements PageTitleConfig {

    private final CardFacadeService cardFacadeService;
    private final BranchFacadeService branchFacadeService;
    private final AccountFacadeService accountFacadeService;
    private final CustomerFacadeService customerFacadeService;

    public CardController(CardFacadeService cardFacadeService, BranchFacadeService branchFacadeService, AccountFacadeService accountFacadeService, CustomerFacadeService customerFacadeService) {
        this.cardFacadeService = cardFacadeService;
        this.branchFacadeService = branchFacadeService;
        this.accountFacadeService = accountFacadeService;
        this.customerFacadeService = customerFacadeService;
    }

    @GetMapping(value = Web.Endpoint.CARDS)
    public String listingCards(Model model) {

        configPageTitle(model, Web.Menu.Card.ROOT);
        model.addAttribute("cards", StatedObjectFormatter.format(cardFacadeService.findAllCards()));
        return Web.View.CARDS;
    }

    @GetMapping(value = Web.Endpoint.CARDS + "/{cardId}")
    public String cardDetails(@PathVariable Long cardId, Model model) {

        CardDto card = cardFacadeService.findCardById(cardId);
        configPageTitle(model, "Card Details");
        model.addAttribute("card", StatedObjectFormatter.format(card));
        return Web.View.CARD_DETAILS;
    }

    @PostMapping(value = Web.Endpoint.CARD_ACTIVATE + "/{cardId}")
    public String activateCard(@PathVariable Long cardId, RedirectAttributes redirect) {

        cardFacadeService.activateCard(cardId);
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Card successfully activated !");

        return String.format("redirect:%s/%d", Web.Endpoint.CARDS, cardId);
    }

    @PostMapping(value = Web.Endpoint.CARD_DEACTIVATE + "/{cardId}")
    public String deactivateCard(@PathVariable Long cardId, @RequestParam String rejectReason, RedirectAttributes redirect) {

        if(rejectReason.isBlank())
            redirect.addFlashAttribute(Web.MessageTag.ERROR, "Deactivate reason is mandatory !");
        else {
            redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Card successfully deactivated!");
            cardFacadeService.deactivateCard(cardId, rejectReason);
        }

        return String.format("redirect:%s/%d", Web.Endpoint.CARDS, cardId);
    }

    @GetMapping(value = Web.Endpoint.CARD_REQUESTS)
    public String listingRequests(Model model, PostCardRequestDto postCardRequestDto) {
        configPageTitle(model, Web.Menu.Card.REQUESTS);
        model.addAttribute("cardRequests", StatedObjectFormatter.format(cardFacadeService.findAllCardRequests()));

        return Web.View.CARD_REQUESTS;
    }

    @GetMapping(value = Web.Endpoint.CARD_REQUESTS + "/{requestId}")
    public String requestDetails(Model model, @PathVariable Long requestId, PostCardDto postCardDto) {

        CardRequestDto requestDto = cardFacadeService.findRequestById(requestId);

        configCardRequestAttributesPage(model, requestDto);
        return Web.View.CARD_REQUEST_DETAILS;
    }


    /*** FOR CARD REQUEST ***/

    @PostMapping(value = Web.Endpoint.CARD_REQUESTS)
    public String sendRequest(Model model, @Valid PostCardRequestDto postCardRequestDto, BindingResult binding, RedirectAttributes redirect) {

        if(binding.hasErrors()) {
            configPageTitle(model, Web.Menu.Card.REQUESTS);
            model.addAttribute(Web.MessageTag.ERROR, "Invalid request application information !");
            return Web.View.CARD_REQUESTS;
        }

        //TODO: get branch, customer and his account
        BranchDto branchDto = branchFacadeService.findAll().get(0);
        CustomerDto customerDto = customerFacadeService.findByIdentity(postCardRequestDto.getCustomerIdentityNumber());
        TrunkDto trunkDto = accountFacadeService.findTrunkByCustomerAndAccountNumber(customerDto, postCardRequestDto.getAccountNumber());

        CardRequestDto requestDto = CardRequestDto.builder()
                .cardType(postCardRequestDto.getCardType())
                .trunk(trunkDto)
                .branch(branchDto)
                .build();
        cardFacadeService.sendRequest(requestDto);
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Card request successfully sent !");

        return "redirect:" + Web.Endpoint.CARD_REQUESTS;
    }

    @PostMapping(value = Web.Endpoint.CARD_REQUEST_APPROVE + "/{requestId}")
    public String approveRequest(Model model, @PathVariable Long requestId, @Valid PostCardDto postCardDto, BindingResult binding, RedirectAttributes redirect) {

        CardRequestDto requestDto = cardFacadeService.findRequestById(requestId);

        if(binding.hasErrors() || !CardExpirationDateUtils.isValidExpiryDate(Integer.parseInt(postCardDto.getExpiryYear()), Integer.parseInt(postCardDto.getExpiryMonth()))) {
            configCardRequestAttributesPage(model, requestDto);
            model.addAttribute(Web.MessageTag.ERROR, "Invalid card information. Check expiration date if no error indication has displayed");
            return Web.View.CARD_REQUEST_DETAILS;
        }


        //TODO: save card information and save it
        CardDto cardDto = CardDto.builder()
                .cardType(postCardDto.getCardType())
                .trunk(requestDto.getTrunk())
                .cardName(postCardDto.getCardName())
                .cardNumber(postCardDto.getCardNumber())
                .expirationDate(CardExpirationDateUtils.getDate(Integer.parseInt(postCardDto.getExpiryYear()), Integer.parseInt(postCardDto.getExpiryMonth())))
                .status(Status.PENDING.code())
                .branch(requestDto.getBranch())
                .failureReason("New added")
                .cvc(postCardDto.getCvc())
                .build();

        cardFacadeService.saveCard(cardDto);


        //TODO: approve the request
        cardFacadeService.approveRequest(requestId);
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Request approved successfully !");

        return "redirect:" + Web.Endpoint.CARD_REQUESTS + "/" + requestId;
    }

    @PostMapping(value = Web.Endpoint.CARD_REQUEST_REJECT + "/{requestId}")
    public String rejectRequest(@PathVariable Long requestId, @RequestParam String rejectReason, RedirectAttributes redirect) {

        if(rejectReason.isBlank())
            redirect.addFlashAttribute(Web.MessageTag.ERROR, "Reject reason reason is mandatory !");
        else {
            redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Card request successfully rejected!");
            cardFacadeService.rejectRequest(requestId, rejectReason);
        }

        return "redirect:" + Web.Endpoint.CARD_REQUESTS + "/" + requestId;
    }

    private void configCardRequestAttributesPage(Model model, CardRequestDto requestDto) {
        configPageTitle(model, "Request Details");
        model.addAttribute("requestDto", StatedObjectFormatter.format(requestDto));
        model.addAttribute("months", CardExpirationDateUtils.getMonths());
        model.addAttribute("years", CardExpirationDateUtils.getYears());
    }

    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Card.MENU, null};
    }
}
