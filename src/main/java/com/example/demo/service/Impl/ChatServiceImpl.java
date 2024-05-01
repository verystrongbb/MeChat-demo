package com.example.demo.service.Impl;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.LuckyMoney;
import com.example.demo.mapper.LuckyMoneyMapper;
import com.example.demo.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatServiceImpl  implements ChatService {
    @Autowired
    private SimpMessageSendingOperations simpMessageSendingOperations;
    @Autowired
    private LuckyMoneyMapper luckyMoneyMapper;

    public void sendMsg(@Payload ChatMessage chatMessage){
        log.info("\nSend msg by simpMSO:\n"+chatMessage.toString());
        //生成chatMessage的id并将信息保存到mysql的Message表中

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
