package com.example.demo.controller;
import com.example.demo.common.JsonUtil;
import com.example.demo.entity.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class ChatController {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Value("${redis.channel.msgToAll}")
    private String msgToAll;
    @Value("${redis.set.onlineUsers}")
    private String onlineUsers;

    @Value("${redis.channel.userStatus}")
    private String userStatus;


    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        try {
            redisTemplate.convertAndSend(msgToAll, JsonUtil.parseObjToJson(chatMessage));
        }catch (Exception e)
        {
            log.info(e.getMessage()+e);
        }
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        log.info("User added in Chatroom:" + chatMessage.getSender());
        try {
            headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
            redisTemplate.opsForSet().add(onlineUsers,chatMessage.getSender());
            redisTemplate.convertAndSend(userStatus,JsonUtil.parseObjToJson(chatMessage));
        }catch (Exception e)
        {
            log.info(e.getMessage()+e);
        }

    }
}

