package com.example.tempfit.controller;

import com.example.tempfit.dto.ChatUserDTO;
import com.example.tempfit.entity.Member;
import com.example.tempfit.entity.Message;
import com.example.tempfit.repository.MemberRepository;
import com.example.tempfit.security.AuthMemberDTO;
import com.example.tempfit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final MemberRepository memberRepository;

    /**
     * 특정 사용자와의 1:1 메시지 대화창
     */
    @GetMapping("/{receiverId}")
    public String viewMessages(@PathVariable String receiverId,
            @AuthenticationPrincipal AuthMemberDTO authMemberDTO,
            Model model) {

        if (authMemberDTO == null) {
            return "redirect:/login"; // 인증 안 된 경우 로그인 페이지로
        }

        String senderId = authMemberDTO.getEmail();

        try {
            Member sender = memberRepository.findByEmail(senderId)
                    .orElseThrow(() -> new IllegalArgumentException("발신자를 찾을 수 없습니다."));
            Member receiver = memberRepository.findByEmail(receiverId)
                    .orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다."));
            model.addAttribute("receiver", receiver);
            model.addAttribute("sender", sender);

        } catch (IllegalArgumentException e) {
            return "redirect:/";
        }

        List<Message> messages = messageService.getConversation(senderId, receiverId);

        // List<Member> partner = messageService.getPartner(senderId);

        model.addAttribute("messages", messages);
        // model.addAttribute("partner", partner);

        return "chat";
    }

    /**
     * 메시지 전송
     */
    @PostMapping("/{receiverId}")
    public String sendMessage(@PathVariable String receiverId,
            @AuthenticationPrincipal AuthMemberDTO senderDTO,
            @RequestParam String content) {

        if (senderDTO == null) {
            return "redirect:/login"; // 인증 안 된 경우
        }

        String senderId = senderDTO.getEmail();

        if (receiverId == null || content.isBlank()) {
            return "redirect:/messages/" + receiverId;
        }

        try {
            messageService.sendMessage(senderId, receiverId, content);
            return "redirect:/messages/" + receiverId;
        } catch (IllegalArgumentException e) {
            return "redirect:/";
        }
    }

    @GetMapping("/chat")
    public String openChatPage(@AuthenticationPrincipal Member user, Model model) {
        List<ChatUserDTO> chatUsers = messageService.getChatPartners(user.getEmail());
        model.addAttribute("chatUsers", chatUsers);
        model.addAttribute("currentUser", user);
        return "chat/chatPage";
    }
}
