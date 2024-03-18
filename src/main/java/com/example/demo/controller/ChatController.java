package com.example.demo.controller;
import com.example.demo.common.JsonUtil;
import com.example.demo.common.R;
import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.User;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
//restcontroller vs controller？后面的R作为reponsebody返回
@RestController

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
    @PostMapping("/login")
    public R<User> login(HttpServletRequest request, @RequestBody User user){

        //1、将页面提交的密码password进行md5加密处理
//        String password = user.getPassword();
//        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名username查询数据库

        //3、如果没有查询到则返回登录失败结果

        //4、密码比对，如果不一致则返回登录失败结果


        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果


        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("username",user.getUsername());
        return R.success(user);
    }
}

