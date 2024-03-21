package com.example.demo.controller;
import com.example.demo.common.JsonUtil;
import com.example.demo.common.R;
import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.MyUser;
import com.example.demo.service.MyUserService;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
@Slf4j
//restcontroller vs controller？后面的R作为reponsebody返回
@RestController

public class ChatController {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private MyUserService myUserService;

    @Value("${spring.redis.channel.msgToAll}")
    private String msgToAll;
    @Value("${spring.redis.set.onlineUsers}")
    private String onlineUsers;

    @Value("${spring.redis.channel.userStatus}")
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
    @Transactional
    public R<MyUser> login(HttpServletRequest request, @RequestBody MyUser myUser){

        //1、将页面提交的密码password进行md5加密处理
        String password = myUser.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<MyUser> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(MyUser::getUsername, myUser.getUsername());
        MyUser usr= myUserService.getOne(queryWrapper);
        //3、如果没有查询到则返回登录失败结果
        if(usr==null){
            return R.error("登陆失败");
        }
        //4、密码比对，如果不一致则返回登录失败结果
        if(!usr.getPassword().equals(password)){
            return R.error("login failed");
        }

        //登录成功，将id存入Session并返回登录成功结果
        request.getSession().setAttribute("username", myUser.getUsername());
        return R.success(myUser);
    }
}

