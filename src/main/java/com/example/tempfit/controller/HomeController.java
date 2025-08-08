package com.example.tempfit.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.tempfit.security.AuthMemberDTO;

@Log4j2
@Controller
public class HomeController {

    @GetMapping("/")
    public String getHome(
            @AuthenticationPrincipal AuthMemberDTO auth,
            Model model
    ) {
        // 로그인 상태면 initialGender 모델에 추가
        if (auth != null) {
            model.addAttribute(
              "initialGender",
              auth.getSex().name().toLowerCase()  // "male" 또는 "female"
            );
        }
        return "main";  // src/main/resources/templates/main.html
    }

    // POST는 gender 세팅할 필요가 없으면 그대로 두셔도 됩니다.
    @PostMapping("/")
    public String postHome() {
        return "main";
    }

    @GetMapping("/test33")
    public void getTest() { }
}
