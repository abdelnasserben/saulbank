package com.dabel.app.web;

import org.springframework.ui.Model;

public interface PageTitleConfig {

    String[] getMenuAndSubMenu();

    default void configPageTitle(Model model, String pageTitle) {
        model.addAttribute("currentPage", pageTitle);
        model.addAttribute("currentMenu", getMenuAndSubMenu()[0]);
        model.addAttribute("currentSubMenu", getMenuAndSubMenu()[1]);
    }
}
