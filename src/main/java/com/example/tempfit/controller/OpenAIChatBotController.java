// src/main/java/com/example/tempfit/controller/OpenAIChatBotController.java
package com.example.tempfit.controller;

import com.example.tempfit.dto.OpenAIChatbotMessageRequest;
import com.example.tempfit.dto.OpenAIChatbotMessageResponse;
import com.example.tempfit.service.OpenAIChatSessionContext;
import com.example.tempfit.service.OpenAIChatbotService;
import com.example.tempfit.repository.MemberRepository;
import com.example.tempfit.entity.Member;
import com.example.tempfit.entity.Sex;

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
    private final MemberRepository memberRepository;

    @GetMapping("/chatbot")
    public String chatbotPage(
            @RequestParam(value = "temp", required = false) Double temp,
            @RequestParam(value = "loc", required = false) String loc,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lon", required = false) Double lon,
            Model model,
            org.springframework.security.core.Authentication authentication) {
        ZoneId kst = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(kst);

        model.addAttribute("ctxTemp", temp);
        model.addAttribute("ctxLoc", loc);
        model.addAttribute("ctxDate", (date != null ? date : today).toString());
        model.addAttribute("ctxLat", lat);
        model.addAttribute("ctxLon", lon);
        model.addAttribute("todayKst", today.toString());

        boolean isLoggedIn = authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(String.valueOf(authentication.getPrincipal()));
        model.addAttribute("isLoggedIn", isLoggedIn);

        return "chatbot";
    }

    @PostMapping(value = "/api/chatbot", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public OpenAIChatbotMessageResponse chat(
            @RequestBody @Valid OpenAIChatbotMessageRequest req,
            @AuthenticationPrincipal(expression = "sex") Integer sessionSex,
            jakarta.servlet.http.HttpSession httpSession,
            org.springframework.security.core.Authentication authentication) {

        // 로그인 강제
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(authentication.getPrincipal()))) {
            return new OpenAIChatbotMessageResponse("로그인 후 이용할 수 있어요.");
        }

        // 성별 코드 주입
        try {
            String email = authentication.getName();
            Member m = memberRepository.findById(email).orElse(null);
            if (m != null && m.getSex() != null && req.getSexCode() == null) {
                // enum → 코드 매핑: 남=0, 여=1
                Integer sexCode = (m.getSex() == Sex.MALE) ? 0 : 1;
                req.setSexCode(sexCode);
            }
        } catch (Exception ignored) {
        }

        // 퍼스널컬러 코드
        try {
            String email = authentication.getName();
            Member m = memberRepository.findById(email).orElse(null);
            if (m != null) {
                req.setPersonalColorCode(m.getPersonalColorCode());
            }
        } catch (Exception ignored) {
        }

        // 세션 컨텍스트 로드/생성
        OpenAIChatSessionContext ctx = (OpenAIChatSessionContext) httpSession.getAttribute("CHAT_CTX");
        if (ctx == null) {
            ctx = new OpenAIChatSessionContext();
            httpSession.setAttribute("CHAT_CTX", ctx);
        }

        // 서비스 호출 및 컨텍스트 저장
        OpenAIChatbotMessageResponse resp = chatService.chat(req, ctx);
        httpSession.setAttribute("CHAT_CTX", ctx);
        return resp;
    }
}
