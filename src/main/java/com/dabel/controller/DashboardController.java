package com.dabel.controller;

import com.dabel.app.web.PageTitleConfig;
import com.dabel.constant.App;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController implements PageTitleConfig {

    @GetMapping
    public String dashboard(Model model) {

        configPageTitle(model, App.Menu.General.DASHBOARD);
        return App.View.DASHBOARD;
    }

    @Override
    public String[] getMenuAndSubMenu() {
        return new String[]{App.Menu.General.MENU, null};
    }
}
