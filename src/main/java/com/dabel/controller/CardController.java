package com.dabel.controller;

import com.dabel.app.CardExpirationDateUtils;
import com.dabel.app.Helper;
import com.dabel.app.StatedObjectFormatter;
import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.BankFees;
import com.dabel.constant.Status;
import com.dabel.constant.Web;
import com.dabel.dto.*;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.card.CardFacadeService;
import com.dabel.service.customer.CustomerFacadeService;
import com.dabel.service.user.UserService;
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
    private final AccountFacadeService accountFacadeService;
    private final CustomerFacadeService customerFacadeService;
    private final UserService userService;
    
    public CardController(CardFacadeService cardFacadeService, AccountFacadeService accountFacadeService, CustomerFacadeService customerFacadeService, UserService userService) {
        this.cardFacadeService = cardFacadeService;
        this.accountFacadeService = accountFacadeService;
        this.customerFacadeService = customerFacadeService;
        this.userService = userService;
    }

    @GetMapping(value = Web.Endpoint.CARDS)
    public String listAllCards(Model model) {

        configPageTitle(model, Web.Menu.Card.ROOT);
        model.addAttribute("cards", StatedObjectFormatter.format(cardFacadeService.getAllCards()));
        return Web.View.CARDS;
    }

    @GetMapping(value = Web.Endpoint.CARDS + "/{cardId}")
    public String showCardDetails(@PathVariable Long cardId, Model model) {

        CardDto card = cardFacadeService.getCardById(cardId);
        configPageTitle(model, "Card Details");
        model.addAttribute("card", StatedObjectFormatter.format(card));
        return Web.View.CARD_DETAILS;
    }

    @PostMapping(value = Web.Endpoint.CARD_ACTIVATE + "/{cardId}")
    public String activateCard(@PathVariable Long cardId, RedirectAttributes redirectAttributes) {

        cardFacadeService.activateCard(cardId);
        redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, "Card successfully activated !");

        return String.format("redirect:%s/%d", Web.Endpoint.CARDS, cardId);
    }

    @PostMapping(value = Web.Endpoint.CARD_DEACTIVATE + "/{cardId}")
    public String deactivateCard(@PathVariable Long cardId, @RequestParam String rejectReason, RedirectAttributes redirectAttributes) {

        if(rejectReason.isBlank())
            redirectAttributes.addFlashAttribute(Web.MessageTag.ERROR, "Deactivate reason is mandatory !");
        else {
            redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, "Card successfully deactivated!");
            cardFacadeService.deactivateCard(cardId, rejectReason);
        }

        return String.format("redirect:%s/%d", Web.Endpoint.CARDS, cardId);
    }


    /*** FOR CARD REQUEST ***/

    @GetMapping(value = Web.Endpoint.CARD_REQUESTS)
    public String listAllCardRequests(Model model, PostCardRequestDto postCardRequestDto) {
        configPageTitle(model, Web.Menu.Card.REQUESTS);
        model.addAttribute("cardRequests", StatedObjectFormatter.format(cardFacadeService.getAllCardRequests()));

        return Web.View.CARD_REQUESTS;
    }

    @GetMapping(value = Web.Endpoint.CARD_REQUESTS + "/{requestId}")
    public String showCardRequestDetails(Model model, @PathVariable Long requestId, PostCardDto postCardDto) {

        CardRequestDto requestDto = cardFacadeService.getCardRequestById(requestId);

        configCardRequestAttributesPage(model, requestDto);
        return Web.View.CARD_REQUEST_DETAILS;
    }

    @PostMapping(value = Web.Endpoint.CARD_REQUESTS)
    public String submitCardRequest(Model model, @Valid PostCardRequestDto postCardRequestDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if(bindingResult.hasErrors()) {
            configPageTitle(model, Web.Menu.Card.REQUESTS);
            model.addAttribute(Web.MessageTag.ERROR, "Invalid request application information !");
            return Web.View.CARD_REQUESTS;
        }

        //TODO: get branch, customer and his account
        CustomerDto customer = customerFacadeService.getByIdentityNumber(postCardRequestDto.getCustomerIdentityNumber());
        TrunkDto trunk = accountFacadeService.getTrunkByCustomerAndNumber(customer, postCardRequestDto.getAccountNumber());

        CardRequestDto requestDto = CardRequestDto.builder()
                .cardType(postCardRequestDto.getCardType())
                .trunk(trunk)
                .applicationFees(BankFees.Basic.CARD_REQUEST)
                .branch(userService.getAuthenticated().getBranch())
                .build();
        cardFacadeService.initCardRequest(requestDto);
        redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, "Card request successfully sent !");

        return "redirect:" + Web.Endpoint.CARD_REQUESTS;
    }

    @PostMapping(value = Web.Endpoint.CARD_REQUEST_APPROVE + "/{requestId}")
    public String approveCardRequest(Model model, @PathVariable Long requestId, @Valid PostCardDto postCardDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        CardRequestDto cardRequest = cardFacadeService.getCardRequestById(requestId);

        if(bindingResult.hasErrors() || !CardExpirationDateUtils.isValidExpiryDate(Integer.parseInt(postCardDto.getExpiryYear()), Integer.parseInt(postCardDto.getExpiryMonth()))) {
            configCardRequestAttributesPage(model, cardRequest);
            model.addAttribute(Web.MessageTag.ERROR, "Invalid card information. Check expiration date if no error indication has displayed");
            return Web.View.CARD_REQUEST_DETAILS;
        }


        //TODO: save card information and save it
        CardDto cardDto = CardDto.builder()
                .cardType(postCardDto.getCardType())
                .trunk(cardRequest.getTrunk())
                .cardName(postCardDto.getCardName())
                .cardNumber(postCardDto.getCardNumber())
                .expirationDate(CardExpirationDateUtils.getDate(Integer.parseInt(postCardDto.getExpiryYear()), Integer.parseInt(postCardDto.getExpiryMonth())))
                .status(Status.PENDING.code())
                .branch(cardRequest.getBranch())
                .failureReason("New added")
                .cvc(postCardDto.getCvc())
                .initiatedBy(Helper.getAuthenticated().getName())
                .build();

        cardFacadeService.saveCard(cardDto);


        //TODO: approve the card request
        cardFacadeService.approveCardRequest(requestId);
        redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, "Request approved successfully !");

        return "redirect:" + Web.Endpoint.CARD_REQUESTS + "/" + requestId;
    }

    @PostMapping(value = Web.Endpoint.CARD_REQUEST_REJECT + "/{requestId}")
    public String rejectCardRequest(@PathVariable Long requestId, @RequestParam String rejectReason, RedirectAttributes redirectAttributes) {

        if(rejectReason.isBlank())
            redirectAttributes.addFlashAttribute(Web.MessageTag.ERROR, "Reject reason is mandatory !");
        else {
            redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, "Card request successfully rejected!");
            cardFacadeService.rejectCardRequest(requestId, rejectReason);
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
