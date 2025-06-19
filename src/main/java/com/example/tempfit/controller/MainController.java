package com.example.tempfit.controller;

import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Log4j2
@RequiredArgsConstructor
@Controller
@RequestMapping("/weather")
public class MainController {

    @GetMapping("/main")
    public void getMain() {
        log.info("페이지 요청");
    }
}
