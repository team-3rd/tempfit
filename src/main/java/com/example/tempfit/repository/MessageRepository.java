package com.example.tempfit.repository;

import com.example.tempfit.entity.Member;
import com.example.tempfit.entity.Message;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long>  {
    
    List<Message> findBySenderAndReceiverOrReceiverAndSenderOrderBySentAt(
        Member sender1, Member receiver1, Member sender2, Member receiver2
    );
}
