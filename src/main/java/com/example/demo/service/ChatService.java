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
        //发给自己看
        if(chatMessage.getTopic()!=null)
        {
            if(!chatMessage.getTopic().equals(chatMessage.getSender())&&!chatMessage.getTopic().equals("public"))
            simpMessageSendingOperations.convertAndSend("/topic/"+chatMessage.getSender(),chatMessage);
            simpMessageSendingOperations.convertAndSend("/topic/"+chatMessage.getTopic(),chatMessage);
        }
        else  simpMessageSendingOperations.convertAndSend("/topic/public",chatMessage);

        }
}
