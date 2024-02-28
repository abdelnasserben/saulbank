package com.dabel.controller;

import com.dabel.constant.App;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping
    public String dashboard(Model model) {

//        setPageTitle(model, "Dashboard", null);
        return App.Web.View.Dashboard.ROOT;
    }
}
