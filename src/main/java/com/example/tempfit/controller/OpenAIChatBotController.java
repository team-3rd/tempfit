package com.example.tempfit.controller;

import com.example.tempfit.dto.OpenAIChatbotMessageRequest;
import com.example.tempfit.dto.OpenAIChatbotMessageResponse;
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

    /**
     * 챗봇 페이지
     * - /chatbot?temp=26&loc=강남구&date=2025-08-11&lat=37.4979&lon=127.0276 형태로 넘기면
     *   hidden 필드에 바인딩되어 JS가 함께 전송.
     */
    @GetMapping("/chatbot")
    public String chatbotPage(
            @RequestParam(value = "temp", required = false) Double temp,
            @RequestParam(value = "loc", required = false) String loc,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lon", required = false) Double lon,
            Model model
    ) {
        ZoneId kst = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(kst);

        LocalDate effectiveDate = (date != null) ? date : today;

        model.addAttribute("ctxTemp", temp);
        model.addAttribute("ctxLoc",  loc);
        model.addAttribute("ctxDate", effectiveDate.toString()); // yyyy-MM-dd
        model.addAttribute("ctxLat",  lat);
        model.addAttribute("ctxLon",  lon);
        model.addAttribute("todayKst", today.toString());

        return "chatbot";
    }

    // 챗봇 API (프론트에서 /api/chatbot 호출)
    @PostMapping(value = "/api/chatbot", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public OpenAIChatbotMessageResponse chat(
            @RequestBody @Valid OpenAIChatbotMessageRequest req,
            @AuthenticationPrincipal(expression = "sex") Integer sessionSex // 필요에 따라 "member.sex" 등으로 변경
    ) {
        if (req.getSexCode() == null && sessionSex != null) {
            req.setSexCode(sessionSex); // DB 값 사용(0/1)
        }
        return chatService.chat(req);
    }
}
