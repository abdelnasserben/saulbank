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
public class ChequeController implements PageTitleConfig {

    private static final String SUCCESS_ACTIVATE_MESSAGE = "Cheque successfully activated!";
    private static final String SUCCESS_DEACTIVATE_MESSAGE = "Cheque successfully deactivated!";
    private static final String SUCCESS_INITIATE_PAYMENT_MESSAGE = "Cheque payment successfully initiated!";
    private static final String SUCCESS_REQUEST_SENT_MESSAGE = "Cheque request successfully sent!";
    private static final String SUCCESS_APPROVE_REQUEST_MESSAGE = "Cheque request successfully approved!";
    private static final String SUCCESS_REJECT_REQUEST_MESSAGE = "Cheque request successfully rejected!";
    private static final String ERROR_DEACTIVATE_REASON_REQUIRED = "Deactivate reason is required!";
    private static final String ERROR_INVALID_INFORMATION = "Invalid information!";
    private static final String ERROR_REJECT_REASON_REQUIRED = "Reject reason is required!";

    private final ChequeFacadeService chequeFacadeService;
    private final TransactionFacadeService transactionFacadeService;

    @Autowired
    public ChequeController(ChequeFacadeService chequeFacadeService, TransactionFacadeService transactionFacadeService) {
        this.chequeFacadeService = chequeFacadeService;
        this.transactionFacadeService = transactionFacadeService;
    }

    /*** FOR CHEQUES ***/

    @GetMapping(value = Web.Endpoint.CHEQUES)
    public String listCheques(Model model) {

        configPageTitle(model, Web.Menu.Cheque.ROOT);
        model.addAttribute("cheques", StatedObjectFormatter.format(chequeFacadeService.findAllCheques()));
        return Web.View.CHEQUES;
    }

    @GetMapping(value = Web.Endpoint.CHEQUES + "/{chequeId}")
    public String showChequeDetails(@PathVariable Long chequeId, Model model) {

        ChequeDto chequeDto = chequeFacadeService.findChequeById(chequeId);
        configPageTitle(model, "Cheque Details");
        model.addAttribute("cheque", StatedObjectFormatter.format(chequeDto));
        return Web.View.CHEQUE_DETAILS;
    }

    @PostMapping(value = Web.Endpoint.CHEQUE_ACTIVATE + "/{chequeId}")
    public String activateCheque(@PathVariable Long chequeId, RedirectAttributes redirectAttributes) {

        chequeFacadeService.activateCheque(chequeId);
        redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, SUCCESS_ACTIVATE_MESSAGE);

        return redirectToChequeDetails(chequeId);
    }

    @PostMapping(value = Web.Endpoint.CHEQUE_DEACTIVATE + "/{chequeId}")
    public String deactivateCheque(@PathVariable Long chequeId, @RequestParam String rejectReason, RedirectAttributes redirectAttributes) {

        if(rejectReason.isBlank())
            redirectAttributes.addFlashAttribute(Web.MessageTag.ERROR, ERROR_DEACTIVATE_REASON_REQUIRED);
        else {
            redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, SUCCESS_DEACTIVATE_MESSAGE);
            chequeFacadeService.deactivateCheque(chequeId, rejectReason);
        }

        return redirectToChequeDetails(chequeId);
    }


    /*** FOR CHEQUES PAYMENTS ***/


    @GetMapping(value = Web.Endpoint.CHEQUE_PAY)
    public String initChequePayment(Model model, PostChequeDto postChequeDto) {
        configPageTitle(model, Web.Menu.Cheque.PAY);

        return Web.View.CHEQUE_PAY;
    }

    @PostMapping(value = Web.Endpoint.CHEQUE_PAY)
    public String processChequePaymentInitialization(Model model, @Valid PostChequeDto postChequeDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if(bindingResult.hasErrors()) {
            configPageTitle(model, Web.Menu.Cheque.PAY);
            model.addAttribute(Web.MessageTag.ERROR, ERROR_INVALID_INFORMATION);

            return Web.View.CHEQUE_PAY;
        }

        //TODO: save init cheque payment
        transactionFacadeService.init(chequeFacadeService.initPay(postChequeDto));

        redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, SUCCESS_INITIATE_PAYMENT_MESSAGE);

        return "redirect:" + Web.Endpoint.CHEQUE_PAY;
    }


    /*** FOR CHEQUES REQUESTS ***/

    @GetMapping(value = Web.Endpoint.CHEQUE_REQUESTS)
    public String listChequeRequests(Model model, PostChequeRequestDto postChequeRequestDto) {

        configPageTitle(model, Web.Menu.Cheque.REQUESTS);
        model.addAttribute("chequeRequests", StatedObjectFormatter.format(chequeFacadeService.findAllRequests()));

        return Web.View.CHEQUE_REQUESTS;
    }

    @PostMapping(value = Web.Endpoint.CHEQUE_REQUESTS)
    public String processChequeRequest(Model model, @Valid PostChequeRequestDto postChequeRequestDto, BindingResult binding, RedirectAttributes redirect) {

        if(binding.hasErrors()) {
            configPageTitle(model, Web.Menu.Cheque.REQUESTS);
            model.addAttribute(Web.MessageTag.ERROR, ERROR_INVALID_INFORMATION);
            return Web.View.CHEQUE_REQUESTS;
        }

        //TODO: save sending request
        chequeFacadeService.sendRequest(postChequeRequestDto);

        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, SUCCESS_REQUEST_SENT_MESSAGE);

        return "redirect:" + Web.Endpoint.CHEQUE_REQUESTS;
    }

    @GetMapping(value = Web.Endpoint.CHEQUE_REQUESTS + "/{requestId}")
    public String showRequestDetails(Model model, @PathVariable Long requestId) {

        ChequeRequestDto requestDto = chequeFacadeService.findRequestById(requestId);

        configPageTitle(model, "Request Details");
        model.addAttribute("requestDto", StatedObjectFormatter.format(requestDto));

        return Web.View.CHEQUES_REQUEST_DETAILS;
    }

    @PostMapping(value = Web.Endpoint.CHEQUE_REQUEST_APPROVE + "/{requestId}")
    public String approveChequeRequest(@PathVariable Long requestId, RedirectAttributes redirect) {

        chequeFacadeService.approveRequest(requestId);
        redirect.addFlashAttribute(Web.MessageTag.SUCCESS, SUCCESS_APPROVE_REQUEST_MESSAGE);

        return redirectToRequestDetails(requestId);
    }

    @PostMapping(value = Web.Endpoint.CHEQUE_REQUEST_REJECT + "/{requestId}")
    public String rejectChequeRequest(@PathVariable Long requestId, @RequestParam String rejectReason, RedirectAttributes redirectAttributes) {

        if(rejectReason.isBlank())
            redirectAttributes.addFlashAttribute(Web.MessageTag.ERROR, ERROR_REJECT_REASON_REQUIRED);
        else {
            redirectAttributes.addFlashAttribute(Web.MessageTag.SUCCESS, SUCCESS_REJECT_REQUEST_MESSAGE);
            chequeFacadeService.rejectRequest(requestId, rejectReason);
        }

        return redirectToRequestDetails(requestId);
    }

    private String redirectToChequeDetails(Long chequeId) {
        return String.format("redirect:%s/%d", Web.Endpoint.CHEQUES, chequeId);
    }

    private String redirectToRequestDetails(Long requestId) {
        return String.format("redirect:%s/%d", Web.Endpoint.CHEQUE_REQUESTS, requestId);
    }

    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.Cheque.MENU, null};
    }
}
