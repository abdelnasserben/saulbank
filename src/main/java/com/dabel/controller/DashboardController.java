package com.dabel.controller;

import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.Web;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController implements PageTitleConfig {

    @GetMapping
    public String dashboard(Model model) {

        configPageTitle(model, Web.Menu.General.DASHBOARD);
        return Web.View.DASHBOARD;
    }

    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{Web.Menu.General.MENU, null};
    }
}
