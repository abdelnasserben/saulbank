package com.dabel.controller;


import com.dabel.app.StatedObjectFormatter;
import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.Web;
import com.dabel.dto.ChequeDto;
import com.dabel.dto.ChequeRequestDto;
import com.dabel.dto.PostChequeDto;
import com.dabel.dto.PostChequeRequestDto;
import com.dabel.service.cheque.ChequeFacadeService;
import com.dabel.service.transaction.TransactionFacadeService;
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
public class ChequeController implements PageTitleConfig {

    private final ChequeFacadeService chequeFacadeService;
    private final TransactionFacadeService transactionFacadeService;

    public ChequeController(ChequeFacadeService chequeFacadeService, TransactionFacadeService transactionFacadeService) {
        this.chequeFacadeService = chequeFacadeService;
        this.transactionFacadeService = transactionFacadeService;
    }

    /*** FOR CHEQUES ***/

    @GetMapping(value = Web.Endpoint.CHEQUES)
    public String listingCheques(Model model) {

        configPageTitle(model, Web.Menu.Cheque.ROOT);
        model.addAttribute("cheques", StatedObjectFormatter.format(chequeFacadeService.findAllCheques()));
        return Web.View.CHEQUES;
    }

    @GetMapping(value = Web.Endpoint.CHEQUES + "/{chequeId}")
    public String chequeDetails(@PathVariable Long chequeId, Model model) {

        ChequeDto chequeDto = chequeFacadeService.findChequeById(chequeId);
        configPageTitle(model, "Cheque Details");
        model.addAttribute("cheque", StatedObjectFormatter.format(chequeDto));
        return Web.View.CHEQUE_DETAILS;
    }

    @PostMapping(value = Web.Endpoint.CHEQUE_ACTIVATE + "/{chequeId}")
    public String activateCheque(@PathVariable Long chequeId, RedirectAttributes redirect) {

        chequeFacadeService.activateCheque(chequeId);
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Cheque successfully activated !");

        return String.format("redirect:%s/%d", Web.Endpoint.CHEQUES, chequeId);
    }

    @PostMapping(value = Web.Endpoint.CHEQUE_DEACTIVATE + "/{chequeId}")
    public String deactivateCheque(@PathVariable Long chequeId, @RequestParam String rejectReason, RedirectAttributes redirect) {

        if(rejectReason.isBlank())
            redirect.addFlashAttribute(Web.MessageTag.ERROR, "Deactivate reason is mandatory !");
        else {
            redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Cheque successfully deactivated!");
            chequeFacadeService.deactivateCheque(chequeId, rejectReason);
        }

        return String.format("redirect:%s/%d", Web.Endpoint.CHEQUES, chequeId);
    }


    /*** FOR CHEQUES PAYMENTS ***/


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
        transactionFacadeService.init(chequeFacadeService.initPay(postChequeDto));

        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Cheque payment successfully initiated!");

        return "redirect:" + Web.Endpoint.CHEQUE_PAY;
    }


    /*** FOR CHEQUES REQUESTS ***/

    @GetMapping(value = Web.Endpoint.CHEQUE_REQUESTS)
    public String listingRequests(Model model, PostChequeRequestDto postChequeRequestDto) {

        configPageTitle(model, Web.Menu.Cheque.REQUESTS);
        model.addAttribute("chequeRequests", StatedObjectFormatter.format(chequeFacadeService.findAllRequests()));

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
        chequeFacadeService.sendRequest(postChequeRequestDto);

        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Cheque request successfully sent !");

        return "redirect:" + Web.Endpoint.CHEQUE_REQUESTS;
    }

    @GetMapping(value = Web.Endpoint.CHEQUE_REQUESTS + "/{requestId}")
    public String requestDetails(Model model, @PathVariable Long requestId) {

        ChequeRequestDto requestDto = chequeFacadeService.findRequestById(requestId);

        configPageTitle(model, "Request Details");
        model.addAttribute("requestDto", StatedObjectFormatter.format(requestDto));

        return Web.View.CHEQUES_REQUEST_DETAILS;
    }

    @PostMapping(value = Web.Endpoint.CHEQUE_REQUEST_APPROVE + "/{requestId}")
    public String approveChequeRequest(@PathVariable Long requestId, RedirectAttributes redirect) {

        chequeFacadeService.approveRequest(requestId);
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Cheque request successfully approved!");

        return "redirect:" + Web.Endpoint.CHEQUE_REQUESTS + "/" + requestId;
    }

    @PostMapping(value = Web.Endpoint.CHEQUE_REQUEST_REJECT + "/{requestId}")
    public String rejectChequeRequest(@PathVariable Long requestId, @RequestParam String rejectReason, RedirectAttributes redirect) {

        if(rejectReason.isBlank())
            redirect.addFlashAttribute(Web.MessageTag.ERROR, "Reject reason is mandatory!");
        else {
            redirect.addFlashAttribute(Web.MessageTag.SUCCESS, "Cheque request successfully rejected!");
            chequeFacadeService.rejectRequest(requestId, rejectReason);
        }

        return "redirect:" + Web.Endpoint.CHEQUE_REQUESTS + "/" + requestId;
    }


    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Cheque.MENU, null};
    }
}
