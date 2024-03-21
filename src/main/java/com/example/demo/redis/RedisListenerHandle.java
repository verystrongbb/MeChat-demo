package com.example.demo.redis;

import com.example.demo.common.JsonUtil;
import com.example.demo.entity.ChatMessage;
import com.example.demo.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisListenerHandle extends MessageListenerAdapter {


    @Value("${spring.redis.channel.msgToAll}")
    private String msgToAll;

    @Value("${spring.redis.channel.userStatus}")
    private String userStatus;

    @Value("${server.port}")
    private String serverPort;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ChatService chatService;

    public void onMessage(Message message,byte[] bytes)
    {
        byte[]body=message.getBody();
        byte[]channel=message.getChannel();
        String rawMsg;
        String topic;
        try {
            rawMsg = redisTemplate.getStringSerializer().deserialize(body);
            topic = redisTemplate.getStringSerializer().deserialize(channel);
            log.info("Recv rawMsg from topic: " + topic + " content: " + rawMsg);
        }catch (Exception e)
        {
            log.info(e.getMessage()+e);
            return;
        }
        if (msgToAll.equals(topic)){
            log.info("Send to all users: "+rawMsg);
            ChatMessage chatMessage =JsonUtil.parseJsonToObj(rawMsg,ChatMessage.class);
            chatService.sendMsg(chatMessage);
        }else if(userStatus.equals(topic)){
            log.info("change user status"+rawMsg);
            ChatMessage chatMessage =JsonUtil.parseJsonToObj(rawMsg,ChatMessage.class);
            chatService.sendMsg(chatMessage);
        }
        else {
            log.warn("no furtehr op on this topic!");
        }
    }



}
