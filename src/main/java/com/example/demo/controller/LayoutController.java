package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LayoutController {
    @RequestMapping("/v2")
    public String getIndexPage() {
        return "v2.html";
    }
}
