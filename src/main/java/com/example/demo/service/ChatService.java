package com.example.demo.service;

import com.example.demo.entity.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatService {
    @Autowired
    private SimpMessageSendingOperations simpMessageSendingOperations;

    public void sendMsg(@Payload ChatMessage chatMessage){
        log.info("\nSend msg by simpMSO:\n"+chatMessage.toString());
        simpMessageSendingOperations.convertAndSend("/topic/"+chatMessage.getTopic(),chatMessage);
    }
}
