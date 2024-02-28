package com.dabel.controller;

import com.dabel.constant.App;
import com.dabel.dto.BranchDto;
import com.dabel.service.branch.BranchFacadeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BranchController {

    @Autowired
    BranchFacadeService branchFacadeService;

    @GetMapping(value = App.Web.Endpoint.Branch.ROOT)
    public String branches(Model model, BranchDto branchDTO) {

//        setTitleAndAddListOfALlBranchesAttribute(model);
        return App.Web.View.Branch.ROOT;
    }

    @PostMapping(value = App.Web.Endpoint.Branch.ROOT)
    public String addNewBranch(Model model, @Valid BranchDto branchDto, BindingResult binding,
                               @RequestParam(required = false, defaultValue = "0") double assetKMF,
                               @RequestParam(required = false, defaultValue = "0") double assetEUR,
                               @RequestParam(required = false, defaultValue = "0") double assetUSD,
                               RedirectAttributes redirect) {

        if(binding.hasErrors() || assetKMF < 0 || assetEUR < 0 || assetUSD < 0) {
//            setTitleAndAddListOfALlBranchesAttribute(model);
            model.addAttribute(App.Web.MessageTag.ERROR, "Invalid information !");
            return App.Web.View.Branch.ROOT;
        }

        double[] vaultsAssets = new double[]{assetKMF, assetEUR, assetUSD};
//        branchFacadeService.create(branchDto, vaultsAssets);
        redirect.addFlashAttribute(App.Web.MessageTag.SUCCESS, "New branch added successfully !");

        return "redirect:" + App.Web.Endpoint.Branch.ROOT;
    }


}
