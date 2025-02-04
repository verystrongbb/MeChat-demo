package com.example.demo.controller;
import com.example.demo.common.JsonUtil;
import com.example.demo.common.R;
import com.example.demo.common.SnowFlakeUtil;
import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.LuckyMoney;
import com.example.demo.entity.MyUser;
import com.example.demo.entity.UserMoney;
import com.example.demo.mapper.LuckyMoneyMapper;
import com.example.demo.mapper.UserMoneyMapper;
import com.example.demo.service.LMService;
import com.example.demo.service.LuckyMoneyService;
import com.example.demo.service.MyUserService;
import com.example.demo.service.UserMoneyService;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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
import java.util.concurrent.TimeUnit;

@Slf4j
//restcontroller vs controller？后面的R作为reponsebody返回
@RestController

public class ChatController {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private MyUserService myUserService;
    @Autowired
    private LuckyMoneyMapper luckyMoneyMapper;
    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    private UserMoneyMapper userMoneyMapper;
    @Autowired
    private LuckyMoneyService luckyMoneyService;
    @Autowired
    private LMService lmService;
    @Autowired
    private RedissonClient redissonClient;

    @Value("${spring.redis.channel.msgToAll}")
    private String msgToAll;
    @Value("${spring.redis.set.onlineUsers}")
    private String onlineUsers;

    @Value("${spring.redis.channel.userStatus}")
    private String userStatus;

    @MessageMapping("/chat.sendMoney")
    public void sendMoney(@Payload ChatMessage chatMessage) {
        SnowFlakeUtil snowFlakeUtil=new SnowFlakeUtil(1,1);
        chatMessage.setId(snowFlakeUtil.getNextId());
        lmService.addLuckyMoney(chatMessage.getId(),chatMessage.getNum());
        ;
        //集群
        try {
            redisTemplate.convertAndSend(msgToAll, JsonUtil.parseObjToJson(chatMessage));
        }catch (Exception e)
        {
            log.info(e.getMessage()+e);
        }
    }
    @MessageMapping("/chat.robMoney")
    public void robMoney(@Payload ChatMessage chatMessage) {
        //TODO:一人一单 基于redission的分布式锁

        lmService.robLuckyMoney(chatMessage);



    }
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

