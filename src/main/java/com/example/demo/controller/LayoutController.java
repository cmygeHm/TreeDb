package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LayoutController {
    @RequestMapping("/")
    public String getIndexPage() {
        return "index.html";
    }
}
