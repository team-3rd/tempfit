package com.example.tempfit.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tempfit.entity.Member;
import com.example.tempfit.entity.Message;
import com.example.tempfit.repository.MemberRepository;
import com.example.tempfit.repository.MessageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final MemberRepository memberRepository;

    public Message sendMessage(String senderId, String receiverId, String content) {
        Member sender = memberRepository.findByEmail(senderId)
                .orElseThrow(() -> new IllegalArgumentException("보내는 사람을 찾을 수 없습니다."));
        Member receiver = memberRepository.findByEmail(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("받는 사람을 찾을 수 없습니다."));

        Message message = Message.create(sender, receiver, content);
        messageRepository.save(message);
        return message;
    }

    @Transactional(readOnly = true)
    public List<Message> getConversation(String user1, String user2) {
        Member member1 = memberRepository.findByEmail(user1)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));
        Member member2 = memberRepository.findByEmail(user2)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));
        return messageRepository.findConversationBetween(user1, user2);
    }
}
