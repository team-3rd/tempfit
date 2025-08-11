package com.example.tempfit.controller;

import com.example.tempfit.service.PersonalColorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PersonalColorController {

    private final PersonalColorService personalColorService;

    /** 퍼스널컬러 선택 페이지 */
    @GetMapping("/personalcolor")
    public String page(Model model, Principal principal) {
        // model.addAttribute("imageUrl", "/images/personalcolor/퍼스널컬러진단테스트.jpg");
        String email = currentUserEmail(principal);
        int current = (email == null) ? 0 : personalColorService.getChoice(email);
        model.addAttribute("currentCode", current);
        return "personalcolor"; // templates/personalcolor.html
    }

    /** 선택 저장 (AJAX) */
    @PostMapping(value = "/personalcolor/select", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> select(@RequestParam("code") int code, Principal principal) {
        String email = currentUserEmail(principal);
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        int saved = personalColorService.saveChoice(email, code);
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("code", saved);
        return res;
    }

    /** 레거시 폼 제출 처리 -> 저장 후 /personalcolor로 리다이렉트 */
    @PostMapping("/personalcolor/result")
    public String legacyResult(@RequestParam(value = "code", required = false) Integer code,
                               Principal principal,
                               RedirectAttributes ra) {
        String email = currentUserEmail(principal);
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        if (code != null) {
            personalColorService.saveChoice(email, code);
            ra.addFlashAttribute("toast", "저장되었습니다");
        }
        return "redirect:/personalcolor";
    }

    /** 내 현재 선택 조회 (옵션) */
    @GetMapping("/api/personalcolor/me")
    @ResponseBody
    public Map<String, Object> myChoice(Principal principal) {
        String email = currentUserEmail(principal);
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        int current = personalColorService.getChoice(email);
        Map<String, Object> res = new HashMap<>();
        res.put("code", current);
        return res;
    }

    private String currentUserEmail(Principal principal) {
        if (principal != null) return principal.getName();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object name = auth.getName();
            return name == null ? null : name.toString();
        }
        return null;
    }
}
