// src/main/java/com/example/tempfit/controller/OpenAIChatBotController.java
package com.example.tempfit.controller;

import com.example.tempfit.dto.OpenAIChatbotMessageRequest;
import com.example.tempfit.dto.OpenAIChatbotMessageResponse;
import com.example.tempfit.service.ChatSessionContext;
import com.example.tempfit.service.OpenAIChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.ui.Model;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RequiredArgsConstructor
@Controller
public class OpenAIChatBotController {

    private final OpenAIChatbotService chatService;

    @GetMapping("/chatbot")
    public String chatbotPage(
            @RequestParam(value = "temp", required = false) Double temp,
            @RequestParam(value = "loc", required = false) String loc,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lon", required = false) Double lon,
            Model model,
            org.springframework.security.core.Authentication authentication // 로그인 여부 확인
    ) {
        ZoneId kst = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(kst);

        LocalDate effectiveDate = (date != null) ? date : today;

        // 로그인 여부 플래그
        boolean isLoggedIn = authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(String.valueOf(authentication.getPrincipal()));

        model.addAttribute("ctxTemp", temp);
        model.addAttribute("ctxLoc",  loc);
        model.addAttribute("ctxDate", effectiveDate.toString()); // yyyy-MM-dd
        model.addAttribute("ctxLat",  lat);
        model.addAttribute("ctxLon",  lon);
        model.addAttribute("todayKst", today.toString());

        // ★ 여기 추가: 로그인 여부를 템플릿/JS로 전달
        model.addAttribute("isLoggedIn", isLoggedIn);

        return "chatbot";
    }

    @PostMapping(value = "/api/chatbot", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public OpenAIChatbotMessageResponse chat(
            @RequestBody @Valid OpenAIChatbotMessageRequest req,
            @AuthenticationPrincipal(expression = "sex") Integer sessionSex, // 남=0, 여=1 (로그인 시)
            jakarta.servlet.http.HttpSession httpSession,
            org.springframework.security.core.Authentication authentication) {

        // 1) 로그인 강제 (API도 방어)
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(authentication.getPrincipal()))) {
            return new OpenAIChatbotMessageResponse("로그인 후 이용할 수 있어요.");
        }

        // 2) 성별 코드가 비었으면 세션(회원) 값 주입
        if (req.getSexCode() == null && sessionSex != null) {
            req.setSexCode(sessionSex);
        }

        // 3) 세션 컨텍스트 로드/생성
        ChatSessionContext ctx = (ChatSessionContext) httpSession.getAttribute("CHAT_CTX");
        if (ctx == null) {
            ctx = new ChatSessionContext();
            httpSession.setAttribute("CHAT_CTX", ctx);
        }

        // 4) 서비스 호출(컨텍스트 넘김) 및 컨텍스트 저장
        OpenAIChatbotMessageResponse resp = chatService.chat(req, ctx);
        httpSession.setAttribute("CHAT_CTX", ctx);
        return resp;
    }
}
