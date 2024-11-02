package com.dabel.controller;

import com.dabel.constant.Web;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageNotFoundController {

    @GetMapping(value = Web.Endpoint.PAGE_404)
    public String sho404Page() {
        return Web.View.PAGE_404;
    }
}