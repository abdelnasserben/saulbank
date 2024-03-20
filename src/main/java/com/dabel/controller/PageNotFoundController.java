package com.dabel.controller;

import com.dabel.constant.App;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageNotFoundController {

    @GetMapping(value = App.Endpoint.PAGE_404)
    public String pageNotFound() {
        return App.View.PAGE_404;
    }
}