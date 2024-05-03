package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.LuckyMoney;

public interface LMService extends IService<LuckyMoney> {
    public void addLuckyMoney(long id, int num);
    public void robLuckyMoney(ChatMessage chatMessage);
}
