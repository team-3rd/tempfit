package com.example.tempfit.repository;

import com.example.tempfit.dto.ChatUserDTO;
import com.example.tempfit.entity.Message;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long>  {
    
    @Query("SELECT m FROM Message m WHERE " +
       "(m.sender.email = :user1 AND m.receiver.email = :user2) OR " +
       "(m.sender.email = :user2 AND m.receiver.email = :user1) " +
       "ORDER BY m.sentAt ASC")
    List<Message> findConversationBetween(@Param("user1") String user1, @Param("user2") String user2);
    
    @Query("""
    SELECT DISTINCT new com.example.tempfit.dto.ChatUserDTO(
        CASE WHEN m.sender.email = :userEmail THEN m.receiver.email ELSE m.sender.email END,
        CASE WHEN m.sender.email = :userEmail THEN m.receiver.nickname ELSE m.sender.nickname END
    )
    FROM Message m
    WHERE m.sender.email = :userEmail OR m.receiver.email = :userEmail
    """)
    List<ChatUserDTO> findChatUsers(@Param("userEmail") String userEmail);
}
