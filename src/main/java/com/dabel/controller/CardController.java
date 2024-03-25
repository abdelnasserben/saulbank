package com.dabel.controller;

import com.dabel.app.StatedObjectFormatter;
import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.Status;
import com.dabel.constant.Web;
import com.dabel.dto.*;
import com.dabel.dto.post.PostCardRequest;
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

import java.time.Clock;
import java.time.LocalDate;

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

    @GetMapping(value = Web.Endpoint.CARD_ROOT)
    public String listingCards(Model model) {

        configPageTitle(model, Web.Menu.Card.ROOT);
        model.addAttribute("cards", StatedObjectFormatter.format(cardFacadeService.findAllCards()));
        return Web.View.CARD_LIST;
    }

    @GetMapping(value = Web.Endpoint.CARD_REQUEST_ROOT)
    public String listingRequests(Model model, PostCardRequest postCardRequest) {
        configPageTitle(model, Web.Menu.Card.REQUEST_ROOT);
        model.addAttribute("cardRequests", StatedObjectFormatter.format(cardFacadeService.findAllCardRequests()));

        return Web.View.CARD_REQUEST_LIST;
    }

    @PostMapping(value = Web.Endpoint.CARD_REQUEST_ROOT)
    public String sendRequest(Model model, @Valid PostCardRequest postCardRequest, BindingResult binding, RedirectAttributes redirect) {

        if(binding.hasErrors()) {
            configPageTitle(model, Web.Menu.Card.REQUEST_ROOT);
            model.addAttribute(Web.MessageTag.ERROR, "Invalid request application information !");
            return Web.View.CARD_REQUEST_LIST;
        }

        //TODO: get branch, customer and his account
        BranchDto branchDto = branchFacadeService.findById(1L);
        CustomerDto customerDto = customerFacadeService.findByIdentity(postCardRequest.getCustomerIdentityNumber());
        TrunkDto trunkDto = accountFacadeService.findAccountByCustomerAndAccount(customerDto, postCardRequest.getAccountNumber());

        CardRequestDto requestDto = CardRequestDto.builder()
                .cardType(postCardRequest.getCardType())
                .trunk(trunkDto)
                .branch(branchDto)
                .build();
        cardFacadeService.sendRequest(requestDto);
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Card request successfully sent !");

        return "redirect:" + Web.Endpoint.CARD_REQUEST_ROOT;
    }

    @GetMapping(value = Web.Endpoint.CARD_REQUEST_ROOT + "/{requestId}")
    public String requestDetails(Model model, @PathVariable Long requestId, CardDto cardDto) {

        CardRequestDto requestDto = cardFacadeService.findRequestById(requestId);

        configPageTitle(model, "Request Details");
        model.addAttribute("requestDto", StatedObjectFormatter.format(requestDto));
        return Web.View.CARD_APPLICATION_DETAILS;
    }

    @PostMapping(value = Web.Endpoint.CARD_REQUEST_APPROVE + "/{requestId}")
    public String approveRequest(@PathVariable Long requestId, @Valid CardDto cardDto, BindingResult binding,
                                            @RequestParam int cardExpiryMonth,
                                            @RequestParam int cardExpiryYear,
                                            RedirectAttributes redirect) {

        CardRequestDto requestDTO = cardFacadeService.findRequestById(requestId);
        cardDto.setCardType(requestDTO.getCardType());

        if(binding.hasErrors()) {
            redirect.addFlashAttribute(Web.MessageTag.ERROR, "Invalid card information !");
            return "redirect:" + Web.Endpoint.CARD_REQUEST_ROOT + "/" + requestId;
        }

        //TODO: check expiration date
        LocalDate expirationDate;
        try {
            expirationDate = LocalDate.of(cardExpiryYear, cardExpiryMonth, LocalDate.MAX.getDayOfMonth());
            if(expirationDate.getYear() <= LocalDate.now(Clock.systemUTC()).getYear())
                throw new IllegalArgumentException();
        } catch (Exception ex) {
            redirect.addFlashAttribute(Web.MessageTag.ERROR, "Invalid expiration date!");
            return "redirect:" + Web.Endpoint.CARD_REQUEST_ROOT + "/" + requestId;
        }

        //TODO: update card information and save it
        cardDto.setAccount(requestDTO.getTrunk().getAccount());
        cardDto.setExpirationDate(expirationDate);
        cardDto.setStatus(Status.PENDING.code());
        cardDto.setBranch(requestDTO.getBranch());
        cardDto.setFailureReason("Added");
        cardFacadeService.saveCard(cardDto);


        //TODO: approve the request
        cardFacadeService.approveRequest(requestId);
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Application approved successfully !");

        return "redirect:" + Web.Endpoint.CARD_REQUEST_ROOT + "/" + requestId;
    }

    @PostMapping(value = Web.Endpoint.CARD_REQUEST_REJECT + "/{requestId}")
    public String rejectRequest(@PathVariable Long requestId, @RequestParam String rejectReason, RedirectAttributes redirect) {

        if(rejectReason.isBlank())
            redirect.addFlashAttribute(Web.MessageTag.ERROR, "Reject reason reason is mandatory !");
        else {
            redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Card request successfully rejected!");
            cardFacadeService.rejectRequest(requestId, rejectReason);
        }

        return "redirect:" + Web.Endpoint.CARD_REQUEST_ROOT + "/" + requestId;
    }

//    @GetMapping(value = Web.Endpoint.EXCHANGE_ROOT + "/{exchangeId}")
//    public String exchangeDetails(@PathVariable Long exchangeId, Model model) {
//
//        ExchangeDto exchange = cardFacadeService.findById(exchangeId);
//
//        configPageTitle(model, "Exchange Details");
//        model.addAttribute("exchange", StatedObjectFormatter.format(exchange));
//        return Web.View.EXCHANGE_DETAILS;
//    }
//
//    @GetMapping(value = Web.Endpoint.EXCHANGE_INIT)
//    public String initExchange(Model model, ExchangeDto exchangeDto) {
//        configPageTitle(model, Web.Menu.Exchange.INIT);
//
//        return Web.View.EXCHANGE_INIT;
//    }

//    @PostMapping(value = Web.Endpoint.EXCHANGE_INIT)
//    public String initExchange(Model model, @Valid ExchangeDto exchangeDto, BindingResult binding, RedirectAttributes redirect) {
//
//        if(binding.hasErrors()) {
//            configPageTitle(model, Web.Menu.Exchange.INIT);
//            return Web.View.EXCHANGE_INIT;
//        }
//
//        //TODO: set branch - We'll replace this automatically by user authenticated
//        BranchDto branchDto = branchFacadeService.findById(1L);
//        exchangeDto.setBranch(branchDto);
//
//        cardFacadeService.init(exchangeDto);
//
//        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Exchange successfully initiated");
//        return "redirect:" + Web.Endpoint.EXCHANGE_INIT;
//    }
//
//    @GetMapping(value = Web.Endpoint.EXCHANGE_APPROVE + "/{exchangeId}")
//    public String approveExchange(@PathVariable Long exchangeId, RedirectAttributes redirect) {
//
//        cardFacadeService.approve(exchangeId);
//        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Exchange successfully approved!");
//
//        return "redirect:" + Web.Endpoint.EXCHANGE_ROOT + "/" + exchangeId;
//    }
//
//    @PostMapping(value = Web.Endpoint.EXCHANGE_REJECT + "/{exchangeId}")
//    public String rejectExchange(@PathVariable Long exchangeId, @RequestParam String rejectReason, RedirectAttributes redirect) {
//
//        if(rejectReason.isBlank())
//            redirect.addFlashAttribute(Web.MessageTag.ERROR, "Reject reason is mandatory!");
//        else {
//            redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Exchange successfully rejected!");
//            cardFacadeService.reject(exchangeId, rejectReason);
//        }
//
//        return "redirect:" + Web.Endpoint.EXCHANGE_ROOT + "/" + exchangeId;
//    }


    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Card.MENU, null};
    }
}
