package com.example.tempfit.controller;

import com.example.tempfit.dto.MessageForm;
import com.example.tempfit.entity.Member;
import com.example.tempfit.entity.Message;
import com.example.tempfit.repository.MemberRepository;
import com.example.tempfit.service.MessageService;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;


@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final MemberRepository memberRepository;

    @MessageMapping("/chat.send") // /app/chat.send 로 전송된 메시지를 처리
    public void sendMessage(@Payload MessageForm chatMessage) {

        // 메시지 저장
        Message savedMessage = messageService.sendMessage(
                chatMessage.getSender(),
                chatMessage.getReceiver(),
                chatMessage.getContent()
        );

        // 브로드캐스트 (수신자에게 메시지 전송)
        chatMessage.setSentAt(savedMessage.getSentAt());
        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiver(),
                "/queue/messages",
                chatMessage
        );

        // 송신자에게도 전송 (자기 화면에서도 바로 반영되도록)
        messagingTemplate.convertAndSendToUser(
                chatMessage.getSender(),
                "/queue/messages",
                chatMessage
        );
    }
}