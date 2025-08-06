package com.example.tempfit.repository;

import com.example.tempfit.entity.Member;
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

    List<Message> findBySenderAndReceiverOrReceiverAndSenderOrderBySentAt(
        Member sender1, Member receiver1, Member sender2, Member receiver2
    );
}
