package com.dabel.controller;

import com.dabel.constant.Web;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PageNotFoundController.class)
@AutoConfigureMockMvc
class PageNotFoundControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void shouldDisplay404Page() throws Exception {
        mockMvc.perform(get(Web.Endpoint.PAGE_404))
                .andExpect(status().is(200))
                .andExpect(content().string(containsString("Seems there is nothing here")))
                .andExpect(content().string(containsString("Return Home")));
    }

}